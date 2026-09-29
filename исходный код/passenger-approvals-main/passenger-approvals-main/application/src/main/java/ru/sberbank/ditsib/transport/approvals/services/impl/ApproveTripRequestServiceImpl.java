package ru.sberbank.ditsib.transport.approvals.services.impl;

import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.approvals.database.dao.TripRequestApprovalRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.dto.TripApproveDTO;
import ru.sberbank.ditsib.transport.approvals.dto.params.EmployeeSearchParams;
import ru.sberbank.ditsib.transport.approvals.mappers.TripApprovalMapper;
import ru.sberbank.ditsib.transport.approvals.messaging.senders.ApproveTripRequestSender;
import ru.sberbank.ditsib.transport.approvals.services.*;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;
import ru.sberbank.ditsib.transport.exceptions.NotImplementedException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static ru.sberbank.ditsib.transport.approvals.util.PageableUtils.addPassengerParamsToPageable;

/**
 * Реализация согласований на заявку на поездку.
 */
@RequiredArgsConstructor
@Component
@Slf4j
@Transactional
class ApproveTripRequestServiceImpl implements ApproveTripRequestService {

    private final TripRequestApprovalRepository approvalRepository;
    private final PositionService positionService;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final RequestDocumentService documentService;
    private final ApproveTripRequestSender approveSender;
    private final LimitReservationResponseService limitReservationResponseService;
    private final EmployeeRightService employeeRightService;
    private final ApprovalsSettingsInjectionService approvalsSettingsInjectionService;
    private final TripApprovalMapper tripApprovalMapper;
    private final SearchService searchService;

    @Override
    public TripRequestApproval findOrCreate(UUID actionId) {
        return approvalRepository.findByActionId(actionId).orElse(new TripRequestApproval());
    }

    @Override
    public Optional<TripRequestApproval> findApprovalByActionId(UUID actionId) {
        return approvalRepository.findByActionId(actionId);
    }

    @Override
    public List<TripRequestApproval> findApprovalByActionIdAndStatuses(UUID actionId, Set<Status> statuses) {
        throw new NotImplementedException();
    }

    @Override
    public void save(TripRequestApproval approval) {
        if (Status.NEW == approval.getStatus()) {
            approval.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        }
        final var passenger = employeeService.get(approval.getPassenger().getId())
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, approval.getPassenger().getId()));
        approval.setPassenger(passenger);
        final var savedApproval = approvalRepository.save(approval);
        searchService.saveApprovalJournalForTripRequestApproval(savedApproval);
        if (Status.APPROVED.equals(savedApproval.getStatus())) {
            return;
        }
        handlingAutoApproving(savedApproval);
    }

    @Override
    public void approve(@NotNull UUID approveId, UUID approvedByUserId) {
        final TripRequestApproval approval = getTripApproval(approveId);
        final Employee employee = employeeRightService.checkEmployeeRights(approvedByUserId, approval);
        if (!APPROVABLE_STATUSES.contains(approval.getStatus())) {
            throw new IllegalStateResponseException(String.format("Cannot approve in %s status",
                    approval.getStatus()));
        }
        // проверяем прикрепление документов для междугородних поездок если необходимость прикрепления включена
        // в настройках согласований (trip_confirmation_document_check)
        if (approval.getTransportType().equals("PUBLIC") &&
                approval.isSuburbTrip() &&
                approvalsSettingsInjectionService.isPublicTrConfirmationDocumentRequired(employee.getId())) {
            checkViews(approval, employee);
        }
        innerApprove(approval, employee.getId());
    }

    /**
     * Проверки для PUBLIC согласований
     */
    private void checkViews(TripRequestApproval approval, Employee approvedByEmployee) {
        documentService.checkViews(approvedByEmployee.getId(), approval.getActionId());
    }

    @Override
    public void cancel(TripRequestApproval approval) {
        approvalRepository.findByActionId(approval.getActionId())
                .filter(a -> CANCELLABLE_STATUSES.contains(a.getStatus()))
                .ifPresent(stored -> stored.setStatus(Status.CANCELLED));
    }

    @Override
    public void handlingAutoApproving(@NotNull UUID actionId) {
        approvalRepository
                .findByActionId(actionId)
                .ifPresent(this::handlingAutoApproving);
    }

    @Override
    public void handlingAutoApproving(Employee changedEmployee) {
        approvalRepository
                .findByActorIdAndStatusIn(changedEmployee.getId(), APPROVABLE_STATUSES)
                .forEach(this::handlingAutoApproving);
    }

    @Override
    public void handlingAutoApproving(Position changedPosition) {
        approvalRepository
                .findByPositionAndStatus(changedPosition.getId(), APPROVABLE_STATUSES)
                .forEach(this::handlingAutoApproving);
    }

    @Override
    public void handlingAutoApproving(Department changedDepartment) {
        approvalRepository
                .findByDepartment(changedDepartment.getId(), APPROVABLE_STATUSES)
                .forEach(this::handlingAutoApproving);
    }

    @Override
    public void decline(UUID approveId, String reason, UUID declinedByUserId) {
        TripRequestApproval approval = getTripApproval(approveId);
        final Employee employee = employeeRightService.checkEmployeeRights(declinedByUserId, approval);
        if (!APPROVABLE_STATUSES.contains(approval.getStatus())) {
            throw new IllegalStateResponseException(String.format("Cannot decline approve in %s status",
                    approval.getStatus()));
        }
        approval.setStatus(Status.DECLINED);
        approval.setReason(reason);
        approval.setApprovedById(employee.getId());
        approval = approvalRepository.save(approval);
        approveSender.sendDecline(approval.getActionId(), employee.getId(), reason);
    }

    @Override
    public Collection<TripApproveDTO> getActiveApprovalsForUser(UUID userId) {
        return getDtoList(getApprovalsWithStatusForUserAndPassengerParams(userId, List.of(Status.NEW, Status.EDITED)));
    }

    private List<TripApproveDTO> getDtoList(Collection<TripRequestApproval> approvals) {
        return approvals.stream().map(approval -> {
            var dto = tripApprovalMapper.toApproveDTO(approval);
            dto.setCoopTrip(((approval.getSharedRideId() != null)));
            return dto;
        }).toList();
    }

    @Override
    public Collection<TripApproveDTO> getClosedApprovalsForUser(UUID userId) {
        return getDtoList(getApprovalsWithStatusForUserAndPassengerParams(userId, List.of(Status.APPROVED, Status.CANCELLED, Status.DECLINED)));
    }

    @Override
    public Page<TripApproveDTO> getActiveApprovalsForUser(UUID userId, Pageable page, EmployeeSearchParams params) {
        var result = getApprovalsWithStatusForUserAndPassengerParams(userId, List.of(Status.NEW, Status.EDITED), params, page);

        return new PageImpl<>(getDtoList(result.getContent()), page, result.getTotalElements());
    }

    @Override
    public Page<TripApproveDTO> getClosedApprovalsForUser(UUID userId, Pageable page) {
        var result = getApprovalsWithStatusesForUser(userId, page, List.of(Status.APPROVED, Status.CANCELLED,
                Status.DECLINED));

        return new PageImpl<>(getDtoList(result.getContent()), page, result.getTotalElements());
    }


    private Page<TripRequestApproval> getApprovalsWithStatusesForUser(
            UUID userId, Pageable page,
            List<Status> statuses
    ) {
        Map<String, Collection<UUID>> allowedDepartments =
                employeeRightService.getAllowedDepartments(userId);

        Set<UUID> departmentIds = allowedDepartments.values().stream().
                flatMap(Collection::stream).
                collect(Collectors.toSet());

        Page<TripRequestApproval> approvalsForDepartments = getApprovalsForDepartments(departmentIds,
                allowedDepartments.keySet(),
                page, statuses);
        List<TripRequestApproval> tripRequestApprovals =
                new ArrayList<>(ApproveService.removeDuplicates(approvalsForDepartments.getContent()));

        return new PageImpl<>(tripRequestApprovals, approvalsForDepartments.getPageable(),
                approvalsForDepartments.getTotalElements());
    }

    private List<TripRequestApproval> getApprovalsWithStatusForUserAndPassengerParams(
            UUID userId, List<Status> statuses
    ) {
        final var allowedDepartments = employeeRightService.getAllowedDepartments(userId);

        final var departmentIds = allowedDepartments.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());

        return getApprovalsForDepartments(departmentIds,
                allowedDepartments.keySet(),
                statuses
        );
    }

    private Page<TripRequestApproval> getApprovalsWithStatusForUserAndPassengerParams(
            UUID userId, List<Status> statuses, EmployeeSearchParams params, Pageable pageable
    ) {
        final var allowedDepartments = employeeRightService.getAllowedDepartments(userId);

        Set<UUID> departmentIds = allowedDepartments.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());

        return getApprovalsForDepartments(departmentIds,
                allowedDepartments.keySet(),
                statuses,
                params,
                pageable);
    }

    /**
     * get approvals for departments and this child that not have any limit
     *
     * @param departmentIds    ids of department
     * @param transportTypeSet null if needs all transport types
     * @param page             true if needs active
     * @return list of approvals
     */
    private Page<TripRequestApproval> getApprovalsForDepartments(
            Set<UUID> departmentIds,
            Set<String> transportTypeSet, Pageable page, List<Status> statuses
    ) {

        boolean empty = transportTypeSet.contains(null);
        return approvalRepository.getByDepartmentsInStatuses(departmentIds, statuses, empty ? null : transportTypeSet,
                page);
    }

    private Page<TripRequestApproval> getApprovalsForDepartments(
            Set<UUID> departmentIds,
            Set<String> transportTypeSet, List<Status> statuses, EmployeeSearchParams params, @NonNull Pageable pageable
    ) {

        final var spec = (Specification<TripRequestApproval>) (root, q, cb) -> {
            final var passenger = root.join(TripRequestApproval_.passenger);
            var predicate = cb.and(
                    root.get(TripRequestApproval_.status).in(statuses),
                    passenger.get(Employee_.departmentId).in(departmentIds)
            );
            if (!transportTypeSet.stream().filter(Objects::nonNull).toList().isEmpty()) {
                predicate = cb.and(
                        predicate,
                        root.get(TripRequestApproval_.transportType).in(transportTypeSet)
                );
            }
            if (params != null) {
                if (params.getFullName() != null) {
                    predicate = cb.and(
                            predicate,
                            cb.like(cb.lower(cb.concat(cb.concat(cb.concat(cb.concat(passenger.get(Employee_.lastName), " "), passenger.get(Employee_.firstName)), " "), passenger.get(Employee_.patronymic))), "%" + params.getFullName().toLowerCase() + "%")
                    );
                }
                if (params.getPersonnelNumber() != null) {
                    predicate = cb.and(
                            predicate,
                            cb.equal(passenger.get(Employee_.personnelNumber), params.getPersonnelNumber())
                    );
                }
            }
            return predicate;
        };
        return approvalRepository.findAll(spec, addPassengerParamsToPageable(pageable, params));
    }

    private List<TripRequestApproval> getApprovalsForDepartments(
            Set<UUID> departmentIds,
            Set<String> transportTypeSet, List<Status> statuses
    ) {
        final var spec = (Specification<TripRequestApproval>) (root, q, cb) -> {
            final var passenger = root.join(TripRequestApproval_.passenger);
            var predicate = cb.and(
                    root.get(TripRequestApproval_.status).in(statuses),
                    passenger.get(Employee_.departmentId).in(departmentIds)
            );
            if (!transportTypeSet.stream().filter(Objects::nonNull).toList().isEmpty()) {
                predicate = cb.and(
                        predicate,
                        root.get(TripRequestApproval_.transportType).in(transportTypeSet)
                );
            }
            return predicate;
        };
        return approvalRepository.findAll(spec);
    }

    private void handlingAutoApproving(TripRequestApproval storedApproval) {
        final var actionId = storedApproval.getActionId();
        log.info("Trying to auto-approve {}", actionId);
        final var employeeId = storedApproval.getActorId();
        final var transportType = storedApproval.getTransportType();
        if (!storedApproval.getStatus().equals(Status.CANCELLED) &&
                approvalsSettingsInjectionService.isAutoApproveByApprovalsSettings(
                        storedApproval.getTariffId(), transportType, storedApproval.getCost(),
                        storedApproval.getPurposeId(), employeeId, actionId)) {
            innerApprove(storedApproval, employeeId);
            return;
        }
        if (isEmployeeLimit(storedApproval)) {
            innerApprove(storedApproval, employeeId);
            return;
        }
        var employee = employeeService.get(employeeId);
        if (employee.isEmpty()) {
            log.error("Auto-approving ({}) - skipping cause the employee {} was not found. Action ID: {}",
                    transportType,
                    employeeId,
                    actionId);
            return;
        }
        final var selfApproved = isSelfApproved(employee.get(), storedApproval);
        final var headOfDepartment = isHeadOfDepartment(employee.get(), storedApproval);
        if (selfApproved || headOfDepartment) {
            final var cause = new StringBuilder();
            if (selfApproved) {
                cause.append("position is self-approved");
            }
            if (headOfDepartment) {
                if (!cause.isEmpty()) {
                    cause.append(" and ");
                }
                cause.append("employee is the head of department");
            }
            log.info("Auto-approving ({}) - succeeded cause the employee {} {}. Action ID: {}", transportType, employeeId, cause, actionId);
            innerApprove(storedApproval, employeeId);
        }
    }

    private boolean isEmployeeLimit(TripRequestApproval approval) {
        var result = limitReservationResponseService.findLimitReservedByEmployeeId(approval.getActionId());
        log.info("Автоаппрув ({}) - согласование {} по наличию личного лимита {} {}",
                approval.getTransportType(),
                result != null ? "пропускается" : "не пропускается",
                Optional.ofNullable(result).map(LimitReservationResponse::getId).map(UUID::toString).orElse(""),
                approval.getActionId());
        return result != null;
    }

    private TripRequestApproval getTripApproval(@NotNull UUID approveId) {
        return approvalRepository.findById(approveId).orElseThrow(() -> new EntityNotFoundException(Approval.class, approveId));
    }

    private boolean isSelfApproved(Employee employee, TripRequestApproval approval) {
        final UUID employeeId = employee.getId();
        if (null == employee.getPositionId()) {
            log.error("Автоаппрув ({}) - согласование пропускается, так как сотрудника {} отсутствует должность {}",
                    approval.getTransportType(),
                    employeeId,
                    approval.getActionId());
            return false;
        }
        final Position position = positionService.get(employee.getPositionId()).orElse(null);
        if (null == position) {
            log.error("Автоаппрув ({}) - согласование пропускается, так как не смогли найти должность {} {}",
                    approval.getTransportType(),
                    employee.getPositionId(),
                    approval.getActionId());
            return false;
        }
        log.info("Автоаппрув ({}) - согласование {} по наличию флага автосогласования в должности {} {}",
                approval.getTransportType(),
                position.isSelfApproved() ? "пропускается" : "не пропускается",
                position.getId(),
                approval.getActionId());
        return position.isSelfApproved();
    }

    private boolean isHeadOfDepartment(Employee employee, TripRequestApproval approval) {
        final UUID employeeId = employee.getId();
        final UUID departmentId = employee.getDepartmentId();
        if (null == departmentId) {
            log.error("Автоаппрув ({}) - согласование пропускается, так как сотрудника {} отсутствует департамент {}",
                    approval.getTransportType(),
                    employeeId,
                    approval.getActionId());
            return false;
        }
        final Department department = departmentService.get(departmentId).orElse(null);
        if (null == department) {
            log.error("Автоаппрув ({}) - согласование пропускается, так как отсутствует департамент {} {}",
                    approval.getTransportType(),
                    departmentId,
                    approval.getActionId());
            return false;
        }
        if (null == department.getDepartmentHeadId()) {
            log.error("Автоаппрув ({}) - согласование пропускается, так как отсутствует глава департамента у {} {}",
                    approval.getTransportType(),
                    departmentId,
                    approval.getActionId());
            return false;
        }
        if (Objects.equals(employeeId, department.getDepartmentHeadId())) {
            log.info("Автоаппрув ({}) - согласование пропускается, так как {} глава департамента {} {}",
                    approval.getTransportType(),
                    employeeId,
                    departmentId,
                    approval.getActionId());
        } else {
            log.info("Автоаппрув ({}) - согласование не пропускается, так как {} не глава департамента {} {}",
                    approval.getTransportType(),
                    employeeId,
                    departmentId,
                    approval.getActionId());
        }
        return Objects.equals(employeeId, department.getDepartmentHeadId());
    }

    private void innerApprove(TripRequestApproval storedApproval, UUID employeeId) {
        storedApproval.setApprovedById(employeeId);
        storedApproval.setStatus(Status.APPROVED);
        final var savedApproval = approvalRepository.save(storedApproval);
        approveSender.sendApproved(savedApproval.getActionId(), employeeId);
    }

    @Override
    public Optional<TripRequestApproval> findSharedRideOwnerApprove(UUID sharedRideId) {
        return approvalRepository.findSharedRideOwner(sharedRideId);
    }
}
