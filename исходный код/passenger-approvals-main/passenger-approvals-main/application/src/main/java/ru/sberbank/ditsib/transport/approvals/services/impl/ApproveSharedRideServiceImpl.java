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
import ru.sberbank.ditsib.transport.approvals.database.dao.SharedRideApprovalRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.dto.SharedRideApproveDTO;
import ru.sberbank.ditsib.transport.approvals.dto.params.EmployeeSearchParams;
import ru.sberbank.ditsib.transport.approvals.mappers.TripApprovalMapper;
import ru.sberbank.ditsib.transport.approvals.messaging.senders.ApproveSharedRideSender;
import ru.sberbank.ditsib.transport.approvals.services.EmployeeService;
import ru.sberbank.ditsib.transport.approvals.services.SearchService;
import ru.sberbank.ditsib.transport.approvals.services.SharedRideApproveService;
import ru.sberbank.ditsib.transport.exceptions.IllegalCallerResponseException;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;
import ru.sberbank.ditsib.transport.exceptions.NotImplementedException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация согласований на присоединение к совместной поездке.
 */
@RequiredArgsConstructor
@Component
@Transactional
@Slf4j
public class ApproveSharedRideServiceImpl implements SharedRideApproveService {
    private final SharedRideApprovalRepository approvalRepository;
    private final EmployeeService employeeService;
    private final ApproveSharedRideSender approveSender;
    private final TripApprovalMapper tripApprovalMapper;
    private final SearchService searchService;
    
    @Override
    public void cancel(SharedRideJoinApproval approval) {
        approvalRepository.findByActionId(approval.getActionId())
                          .filter(a -> CANCELLABLE_STATUSES.contains(a.getStatus()))
                          .ifPresent(stored -> stored.setStatus(Status.CANCELLED));
    }
    
    @Override
    public SharedRideJoinApproval findOrCreate(UUID actionId) {
        return approvalRepository.findByActionId(actionId).orElse(new SharedRideJoinApproval());
    }
    
    @Override
    public Optional<SharedRideJoinApproval> findApprovalByActionId(UUID actionId) {
        return approvalRepository.findByActionId(actionId);
    }
    
    @Override
    public List<SharedRideJoinApproval> findApprovalByActionIdAndStatuses(UUID actionId, Set<Status> statuses) {
        throw new NotImplementedException();
    }
    
    @Override
    public void save(SharedRideJoinApproval approval) {
        if (Status.NEW == approval.getStatus()) {
            approval.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        }
        final var savedApproval = approvalRepository.save(approval);
        searchService.saveApprovalJournalForSharedRideJoinApproval(savedApproval);
    }
    
    @Override
    public void approve(@NotNull UUID approveId, UUID approvedByUserId) {
        var employee = employeeService.getByUserId(approvedByUserId);
        final SharedRideJoinApproval approval = getApproval(approveId);
        checkRights(employee, approval);
        if (!APPROVABLE_STATUSES.contains(approval.getStatus())) {
            throw new IllegalStateResponseException(String.format("Cannot approve in %s status",
                                                                  approval.getStatus()));
        }
        innerApprove(approval, employee.getId());
    }
    
    @Override
    public void decline(UUID approveId, String reason, UUID declinedByUserId) {
        var employee = employeeService.getByUserId(declinedByUserId);
        var approval = getApproval(approveId);
        checkRights(employee, approval);
        if (!APPROVABLE_STATUSES.contains(approval.getStatus())) {
            throw new IllegalStateResponseException(String.format("Cannot decline approve in %s status",
                                                                  approval.getStatus()));
        }
        approval.setStatus(Status.DECLINED);
        approval.setReason(reason);
        approval.setApprovedById(employee.getId());
        approval = approvalRepository.save(approval);
        approveSender.sendDecline(approval, reason);
    }
    
    private void checkRights(Employee employee, SharedRideJoinApproval approval) {
        if (!employee.getId().equals(approval.getApprovedById())) {
            throw new IllegalCallerResponseException();
        }
    }
    
    @Override
    public List<SharedRideApproveDTO> getActiveApprovalsForUser(UUID userId) {
        return getDtoList(approvalRepository.findForUserInStatuses(userId, List.of(Status.NEW, Status.EDITED)));
    }
    
    private List<SharedRideApproveDTO> getDtoList(Collection<SharedRideJoinApproval> approvals) {
        return approvals.stream()
                        .map(tripApprovalMapper::toApproveDTO)
                        .collect(Collectors.toList());
    }
    
    //TODO TRANSPORT-6640 реализовать поиск по фильтрам и сортировке пассажира
    @Override
    public Page<SharedRideApproveDTO> getActiveApprovalsForUser(UUID userId, Pageable page, EmployeeSearchParams params) {
        var approvals = getDtoList(approvalRepository.findForUserInStatuses(userId, List.of(Status.NEW, Status.EDITED)));
        return new PageImpl<>(approvals, page, approvals.size());
    }
    
    @Override
    public Page<SharedRideApproveDTO> getClosedApprovalsForUser(UUID userId, Pageable page) {
        var approvals = getClosedApprovalsForUser(userId);
        return new PageImpl<>(approvals, page, approvals.size());
    }
    
    @Override
    public List<SharedRideApproveDTO> getClosedApprovalsForUser(UUID userId) {
        return getDtoList(approvalRepository.findForUserInStatuses(userId, List.of(Status.DECLINED, Status.CANCELLED,
                                                                                   Status.APPROVED)));
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

    private SharedRideJoinApproval getApproval(UUID approveId) {
        return approvalRepository.findById(approveId).orElseThrow(() -> new EntityNotFoundException(Approval.class, approveId));
    }
    
    private void innerApprove(SharedRideJoinApproval approval, UUID employeeId) {
        approval.setApprovedById(employeeId);
        approval.setStatus(Status.APPROVED);
        approveSender.sendApproved(approval);
    }
    
    @Override
    public Optional<SharedRideJoinApproval> getByAddedRequestId(UUID addedRequestId) {
        return approvalRepository.getByAddRequestId(addedRequestId);
    }
}
