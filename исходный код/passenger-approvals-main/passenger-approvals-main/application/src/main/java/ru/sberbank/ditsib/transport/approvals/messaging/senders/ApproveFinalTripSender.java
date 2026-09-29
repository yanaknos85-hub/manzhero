package ru.sberbank.ditsib.transport.approvals.messaging.senders;

import java.util.UUID;

/**
 * Sender final trip approve data.
 */
public interface ApproveFinalTripSender {
    /**
     * Send approve data.
     * @param requestId ID of request
     * @param approvedByEmployeeId
     */
    void sendApproved(UUID requestId, UUID approvedByEmployeeId);

    /**
     * Send decline data.
     * @param requestId ID of request
     * @param declinedByEmployeeId
     * @param reason
     */
    void sendDecline(UUID requestId, UUID declinedByEmployeeId, String reason);
}
