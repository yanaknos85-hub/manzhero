package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sberbank.ditsib.transport.approvals.database.model.Approval;
import ru.sberbank.ditsib.transport.approvals.database.model.Status;
import ru.sberbank.ditsib.transport.approvals.database.model.UpdateTripRequestApproval;
import ru.sberbank.ditsib.transport.approvals.mappers.TripApprovalMapper;
import ru.sberbank.ditsib.transport.approvals.messaging.listeners.UpdateTripRequestListener;
import ru.sberbank.ditsib.transport.approvals.messaging.message.UpdateTripRequestMessage;
import ru.sberbank.ditsib.transport.approvals.services.ApproveUpdateTripRequestService;
import ru.sberbank.ditsib.transport.approvals.services.EmployeeService;

import java.util.Objects;
import java.util.UUID;

/**
 * Реализация слушателя заявок на поездку.
 */
@RequiredArgsConstructor
@Slf4j
public class UpdateTripRequestListenerImpl implements UpdateTripRequestListener {
    private final ApproveUpdateTripRequestService approveService;
    private final TripApprovalMapper mapper;
    private final EmployeeService employeeService;

    @Override
    public void handle(UpdateTripRequestMessage message) {
        try {
            final UUID updateId = message.getId();
            Objects.requireNonNull(updateId, "Request message id is null");
            final UpdateTripRequestApproval approval = approveService.findByUpdateIdOrCreate(updateId);
            approval.setUpdateId(updateId);
            if (message.isDeleted()) {
                if (!isNew(approval)) {
                    approveService.cancel(approval);
                }
                return;
            }
            if (!isNew(approval) && (Status.NEW == approval.getStatus())) {
                approval.setStatus(Status.EDITED);
            }
            mapper.toBaseRequestApproval(message.getRequest(), approval);
            approval.setPassenger(employeeService.getByUserId(message.getRequest().getPassenger().getUserId()));
            approveService.save(approval);
        } catch (Exception e) {
            log.error("Unable save request message: " + e.getMessage(), e);
        }
    
    }
    
    private static boolean isNew(Approval approval) {
        return approval.getId() == null;
    }
    
}
