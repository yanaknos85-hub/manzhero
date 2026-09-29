package ru.sber.transport.approvals.messaging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

/**
 * @param requestId       actor ID
 * @param approved        approved
 * @param actorEmployeeId approved/declined by
 * @param message         message (for instance decline reason)
 */
public record ApproveFinalTripMessage(
        UUID requestId,
        Boolean approved,
        UUID actorEmployeeId,
        String message
) implements Message<UUID> {

    @JsonIgnore
    @Override
    public UUID getId() {
        return requestId;
    }
}
