package ru.sberbank.ditsib.transport.approvals.messaging.senders;

import ru.sberbank.ditsib.transport.approvals.database.model.Approver;

import java.util.Collection;
import java.util.UUID;

/**
 * Sender department approvers
 */
public interface DepartmentTripRequestApproversSender {
    
    void send(UUID departmentId, Collection<Approver> approvers);
    
}
