package ru.sber.transport.approvals.messaging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.sber.transport.messaging.Message;

import java.util.List;
import java.util.UUID;

/**
 * @param actionId        actor ID
 * @param approved        approved. Значение NULL в случае, если сообщение означает создание объекта согласования.
 * @param actorEmployeeId approved/declined by
 * @param message         сообщение (в случае отклонения заявки)
 * @param approverIds     Если approved NULL, то отправляется список с ID сотрудников, которые могут согласовать заявку
 */
public record ApproveTripRequestMessage(
        UUID actionId,
        Boolean approved,
        UUID actorEmployeeId,
        String message,
        List<UUID> approverIds

) implements Message<UUID> {

    @JsonIgnore
    @Override
    public UUID getId() {
        return actionId;
    }
}
