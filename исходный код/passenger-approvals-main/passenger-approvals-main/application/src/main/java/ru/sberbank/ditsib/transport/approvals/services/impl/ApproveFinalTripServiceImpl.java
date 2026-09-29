package ru.sberbank.ditsib.transport.approvals.services.impl;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.approvals.database.dao.FinalTripApprovalRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.dto.TripApproveDTO;
import ru.sberbank.ditsib.transport.approvals.dto.params.EmployeeSearchParams;
import ru.sberbank.ditsib.transport.approvals.mappers.TripApprovalMapper;
import ru.sberbank.ditsib.transport.approvals.messaging.senders.ApproveFinalTripSender;
import ru.sberbank.ditsib.transport.approvals.services.*;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;
import ru.sberbank.ditsib.transport.exceptions.NotImplementedException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация согласований на завершение поездки.
 */
@RequiredArgsConstructor
@Transactional
@Component
@Slf4j
public class ApproveFinalTripServiceImpl implements ApproveService<FinalTripApproval> {
    
    private final FinalTripApprovalRepository approvalRepository;
    private final ApproveFinalTripSender approveSender;
    private final EmployeeRightService employeeRightService;
    private final RequestDocumentService documentService;
    private final ApprovalsSettingsInjectionService approvalsSettingsInjectionService;
    private final TripApprovalMapper tripApprovalMapper;
    private final SearchService searchService;
    
    @Override
    public FinalTripApproval findOrCreate(UUID actionId) {
        return approvalRepository.findByActionId(actionId).orElse(new FinalTripApproval());
    }
    
    @Override
    public void cancel(FinalTripApproval approval) {
        approvalRepository.findByActionId(approval.getActionId())
                          .filter(a -> CANCELLABLE_STATUSES.contains(a.getStatus()))
                          .ifPresent(stored -> stored.setStatus(Status.CANCELLED));
    }
    
    @Override
    public Optional<FinalTripApproval> findApprovalByActionId(UUID actionId) {
        return approvalRepository.findByActionId(actionId);
    }
    
    @Override
    public List<FinalTripApproval> findApprovalByActionIdAndStatuses(UUID actionId, Set<Status> statuses) {
        throw new NotImplementedException();
    }
    
    @Override
    public void save(FinalTripApproval approval) {
        if (Status.NEW == approval.getStatus()) {
            approval.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        }
        final var savedApproval = approvalRepository.save(approval);
        searchService.saveApprovalJournalForFinalTripApproval(savedApproval);
        if ((Status.APPROVED == savedApproval.getStatus())) {
            return;
        }
        handlingAutoApproving(savedApproval);
    }
    
    @Override
    public void approve(@NotNull UUID approveId, UUID approvedByUserId) {
        final FinalTripApproval approval = getApproval(approveId);
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
    private void checkViews(FinalTripApproval approval, Employee approvedByEmployee) {
        documentService.checkViews(approvedByEmployee.getId(), approval.getActionId());
    }
    
    private FinalTripApproval getApproval(UUID approveId) {
        return approvalRepository.findById(approveId).orElseThrow(() -> new EntityNotFoundException(Approval.class, approveId));
    }
    
    @Override
    public void handlingAutoApproving(@NotNull UUID actionId) {
        approvalRepository.findByActionId(actionId).ifPresent(this::handlingAutoApproving);
    }
    
    @Override
    public void handlingAutoApproving(Employee changedEmployee) {
    
    }
    
    @Override
    public void handlingAutoApproving(Position changedPosition) {
    
    }
    
    @Override
    public void handlingAutoApproving(Department changedDepartment) {
    
    }
    
    @Override
    public void decline(UUID approveId, String reason, UUID declinedByUserId) {
        var approval = getApproval(approveId);
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
    public List<TripApproveDTO> getActiveApprovalsForUser(UUID userId) {
        return getDtoList(getApprovalsWithStatusForUser(userId, List.of(Status.EDITED, Status.NEW)));
    }
    
    
    //TODO TRANSPORT-6640 Реализовать поиск и сортировку по параметрам
    @Override
    public Page<TripApproveDTO> getActiveApprovalsForUser(UUID userId, Pageable page, EmployeeSearchParams params) {
        var approvals = getActiveApprovalsForUser(userId);
        return new PageImpl<>(approvals, page, approvals.size());
    }
    
    @Override
    public Page<TripApproveDTO> getClosedApprovalsForUser(UUID userId, Pageable page) {
        var approvals = getClosedApprovalsForUser(userId);
        return new PageImpl<>(approvals, page, approvals.size());
    }
    
    private List<TripApproveDTO> getDtoList(Collection<FinalTripApproval> approvals) {
        return approvals.stream()
                        .map(tripApprovalMapper::toApproveDTO)
                        .collect(Collectors.toList());
    }
    
    
    @Override
    public List<TripApproveDTO> getClosedApprovalsForUser(UUID userId) {
        return getDtoList(getApprovalsWithStatusForUser(userId, List.of(Status.APPROVED, Status.DECLINED, Status.CANCELLED)));
    }

    private List<FinalTripApproval> getApprovalsWithStatusForUser(UUID userId, List<Status> statuses) {
        var result = employeeRightService.getAllowedDepartments(userId).entrySet().stream()
                                         .map(entry -> {
                                             var transportType = entry.getKey();
                                             var departmentIds = entry.getValue();
                                             return getApprovalsForDepartments(
                                                     departmentIds,
                                                     transportType,
                                                     statuses);
                                         })
                                         .flatMap(Collection::stream)
                                         .collect(Collectors.toList());
        return ApproveService.removeDuplicates(result);
    }
    
    /**
     * get approvals for departments and this child that not have any limit
     *
     * @param departmentIds ids of department
     * @param transportType null if needs all transport types
     *
     * @return list of approvals
     */
    private Collection<FinalTripApproval> getApprovalsForDepartments(
            Collection<UUID> departmentIds,
            String transportType, List<Status> statuses
                                                                    ) {
        if (departmentIds.isEmpty()) {
            return new ArrayList<>();
        }
        return approvalRepository.getByDepartmentsInStatuses(departmentIds, statuses, transportType);
    }
    
    private void handlingAutoApproving(FinalTripApproval storedApproval) {
        // если все точки согласованы, или отключен этап утверждения
        if ((storedApproval.getTransportType().equals("PUBLIC")
             && !approvalsSettingsInjectionService.isPublicTrAffirmativeRequired(storedApproval.getActorId()))
            ||
            !storedApproval.getTransportType().equals("PUBLIC")
            && !storedApproval.getTransportType().equals("TAXI")
            && !approvalsSettingsInjectionService.isOtherTrFinalTripConfirmation(
                    storedApproval.getActorId(), storedApproval.getTransportType())) {
            innerApprove(storedApproval, storedApproval.getActorId());
        }
    }

    private void innerApprove(FinalTripApproval storedApproval, UUID employeeId) {
        storedApproval.setApprovedById(employeeId);
        storedApproval.setStatus(Status.APPROVED);
        storedApproval.setApprovalDate(LocalDateTime.now());
        approveSender.sendApproved(storedApproval.getActionId(), employeeId);
    }
    
}
