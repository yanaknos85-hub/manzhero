package ru.sber.transport.approvals.messaging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

/**
 * @param updateId             trip update ID
 * @param approved             approved
 * @param approvedByEmployeeId approved/declined by
 * @param message              message (for instance decline reason)
 */
public record ApproveUpdateTripRequestMessage(
        UUID updateId,
        Boolean approved,
        UUID approvedByEmployeeId,
        String message
) implements Message<UUID> {

    @JsonIgnore
    @Override
    public UUID getId() {
        return updateId;
    }
}

