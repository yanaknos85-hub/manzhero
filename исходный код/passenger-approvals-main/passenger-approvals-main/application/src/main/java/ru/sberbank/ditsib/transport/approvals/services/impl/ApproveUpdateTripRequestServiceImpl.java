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
import ru.sberbank.ditsib.transport.approvals.database.dao.UpdateTripRequestApprovalRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.dto.TripApproveDTO;
import ru.sberbank.ditsib.transport.approvals.dto.params.EmployeeSearchParams;
import ru.sberbank.ditsib.transport.approvals.mappers.TripApprovalMapper;
import ru.sberbank.ditsib.transport.approvals.messaging.senders.ApproveUpdateTripRequestSender;
import ru.sberbank.ditsib.transport.approvals.services.ApproveService;
import ru.sberbank.ditsib.transport.approvals.services.ApproveUpdateTripRequestService;
import ru.sberbank.ditsib.transport.approvals.services.EmployeeRightService;
import ru.sberbank.ditsib.transport.approvals.services.SearchService;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация согласований на изменение маршрута поездки.
 */
@RequiredArgsConstructor
@Component
@Transactional
@Slf4j
public class ApproveUpdateTripRequestServiceImpl implements ApproveUpdateTripRequestService {
    
    private final UpdateTripRequestApprovalRepository approvalRepository;
    private final ApproveUpdateTripRequestSender approveSender;
    private final EmployeeRightService employeeRightService;
    private final TripApprovalMapper tripApprovalMapper;
    private final SearchService searchService;
    
    @Override
    public UpdateTripRequestApproval findOrCreate(UUID actionId) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public UpdateTripRequestApproval findByUpdateIdOrCreate(UUID updateId) {
        return approvalRepository.findByUpdateId(updateId).orElse(new UpdateTripRequestApproval());
    }
    
    @Override
    public void cancelByRequestId(UUID requestId) {
        approvalRepository.getByActionIdAndStatusIn(requestId, CANCELLABLE_STATUSES)
                          .forEach(a -> a.setStatus(Status.CANCELLED));
    }
    
    @Override
    public void cancel(UpdateTripRequestApproval approval) {
        approvalRepository.findByUpdateId(approval.getUpdateId())
                          .filter(a -> CANCELLABLE_STATUSES.contains(a.getStatus()))
                          .ifPresent(stored -> stored.setStatus(Status.CANCELLED));
    }
    
    @Override
    public Optional<UpdateTripRequestApproval> findApprovalByActionId(UUID actionId) {
        return approvalRepository.findByActionId(actionId);
    }
    
    @Override
    public List<UpdateTripRequestApproval> findApprovalByActionIdAndStatuses(UUID actionId, Set<Status> statuses) {
        return approvalRepository.getByActionIdAndStatusIn(actionId, statuses);
    }
    
    @Override
    public void save(UpdateTripRequestApproval approval) {
        if (Status.NEW == approval.getStatus()) {
            approval.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        }
        final var savedApproval = approvalRepository.save(approval);
        searchService.saveApprovalJournalForUpdateTripRequestApproval(savedApproval);
    }
    
    @Override
    public void approve(@NotNull UUID approveId, UUID approvedByUserId) {
        final UpdateTripRequestApproval approval = getApproval(approveId);
        final Employee employee = employeeRightService.checkEmployeeRights(approvedByUserId, approval);
        if (!APPROVABLE_STATUSES.contains(approval.getStatus())) {
            throw new IllegalStateResponseException(String.format("Cannot approve in %s status",
                                                                  approval.getStatus()));
        }
        innerApprove(approval, employee.getId());
    }
    
    private UpdateTripRequestApproval getApproval(UUID approveId) {
        return approvalRepository.findById(approveId).orElseThrow(() -> new EntityNotFoundException(Approval.class, approveId));
    }

    @Override
    public void handlingAutoApproving(@NotNull UUID actionId) {

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
        approveSender.sendDecline(approval.getUpdateId(), employee.getId(), reason);
    }
    
    @Override
    public List<TripApproveDTO> getActiveApprovalsForUser(UUID userId) {
        return getDtoList(getApprovalsWithStatusForUser(userId, List.of(Status.NEW, Status.EDITED)));
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
    
    @Override
    public List<TripApproveDTO> getClosedApprovalsForUser(UUID userId) {
        return getDtoList(getApprovalsWithStatusForUser(userId, List.of(Status.CANCELLED, Status.DECLINED, Status.APPROVED)));
    }

    private List<UpdateTripRequestApproval> getApprovalsWithStatusForUser(UUID userId, List<Status> statuses) {
        var result = employeeRightService.getAllowedDepartments(userId).entrySet().stream()
                                         .map(entry -> {
                                             var transportType = entry.getKey();
                                             var departmentIds = entry.getValue();
                                             return getApprovalsForDepartments(
                                                     statuses,
                                                     departmentIds,
                                                     transportType);
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
    private Collection<UpdateTripRequestApproval> getApprovalsForDepartments(
            Collection<Status> statuses,
            Collection<UUID> departmentIds,
            String transportType
                                                                            ) {
        if (departmentIds.isEmpty()) {
            return new ArrayList<>();
        }
        return approvalRepository.getByDepartmentsInStatuses(departmentIds, statuses, transportType);
    }
    
    private List<TripApproveDTO> getDtoList(Collection<UpdateTripRequestApproval> approvals) {
        return approvals.stream()
                        .map(tripApprovalMapper::toApproveDTO)
                        .collect(Collectors.toList());
    }
    
    private void innerApprove(UpdateTripRequestApproval storedApproval, UUID employeeId) {
        storedApproval.setApprovedById(employeeId);
        storedApproval.setStatus(Status.APPROVED);
        approveSender.sendApproved(storedApproval.getUpdateId(), employeeId);
    }
}
