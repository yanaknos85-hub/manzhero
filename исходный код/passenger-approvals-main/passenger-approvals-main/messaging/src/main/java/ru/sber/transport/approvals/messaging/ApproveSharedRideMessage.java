package ru.sber.transport.approvals.messaging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.sber.transport.messaging.Message;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @param addRequestId         Id заявки, которая присоединяется к совместной
 * @param ownerRequestId       Id заявки-владельца совместной поездки
 * @param approved             approved
 * @param approvedByEmployeeId approved/declined by
 * @param desiredDate
 * @param statusApprove
 * @param message              message (for instance decline reason)
 */
public record ApproveSharedRideMessage(
        UUID addRequestId,
        UUID ownerRequestId,
        Boolean approved,
        UUID approvedByEmployeeId,
        LocalDateTime desiredDate,
        String statusApprove,
        String message
) implements Message<UUID> {

    @JsonIgnore
    @Override
    public UUID getId() {
        return addRequestId;
    }
}

