package ru.sberbank.ditsib.transport.approvals.messaging.senders;

import java.util.List;
import java.util.UUID;

/**
 * Sender trip approve data.
 */
public interface ApproveTripRequestSender {
    
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
    
    /**
     * Отправить уведомление о полученном согласовании.
     * @param actionId идентификатор действия, породившее согласование.
     * @param approverIds ID пользователей с правом на согласование данной заявки
     */
    void send(UUID actionId, List<UUID> approverIds);
}
