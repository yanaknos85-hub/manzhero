package ru.sberbank.ditsib.transport.approvals.messaging.senders;

import java.util.UUID;

/**
 * Sender update trip approve data.
 */
public interface ApproveUpdateTripRequestSender {
    /**
     * Send approve data.
     * @param updateId ID of request
     * @param approvedByEmployeeId
     */
    void sendApproved(UUID updateId, UUID approvedByEmployeeId);
    
    /**
     * Send decline data.
     * @param updateId ID of request
     * @param declinedByEmployeeId
     * @param reason
     */
    void sendDecline(UUID updateId, UUID declinedByEmployeeId, String reason);
}
