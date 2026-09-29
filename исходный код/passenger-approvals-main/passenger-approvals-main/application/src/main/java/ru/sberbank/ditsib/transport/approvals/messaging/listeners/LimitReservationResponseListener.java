package ru.sberbank.ditsib.transport.approvals.messaging.listeners;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import ru.sberbank.ditsib.transport.approvals.messaging.message.LimitActionResultMessage;

import java.util.function.Consumer;

/**
 * Listener of limit reservation response messages.
 */
public interface LimitReservationResponseListener extends Consumer<Message<LimitActionResultMessage>> {
    
    /**
     * Handle limit reservation response message.
     *
     * @param message message.
     */
    void handleLimitReservationResponse(@Payload LimitActionResultMessage message);

    default void accept(Message<LimitActionResultMessage> message) {
        handleLimitReservationResponse(message.getPayload());
    }

}
