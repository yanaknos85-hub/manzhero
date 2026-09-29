package ru.sberbank.ditsib.transport.approvals.messaging.senders;

import ru.sberbank.ditsib.transport.approvals.database.model.SharedRideJoinApproval;


/**
 * Sender shared ride approve data.
 */
public interface ApproveSharedRideSender {
    /**
     * Send approve data.
     * @param approval
     */
    void sendApproved(SharedRideJoinApproval approval);
    
    /**
     * Send decline data.
     * @param approval
     * @param reason
     */
    void sendDecline(SharedRideJoinApproval approval, String reason);
}
