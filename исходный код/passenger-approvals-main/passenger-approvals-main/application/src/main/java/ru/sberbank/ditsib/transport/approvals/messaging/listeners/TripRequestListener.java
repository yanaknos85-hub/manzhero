package ru.sberbank.ditsib.transport.approvals.messaging.listeners;

import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestMessage;

import java.util.function.Consumer;

/**
 * Слушатель заявок на поездки.
 */
public interface TripRequestListener extends Consumer<Message<RequestMessage>> {
    
    /**
     * Получено новое сообщение.
     *
     * @param message сообщение.
     */
    void handle(RequestMessage message);

    default void accept(Message<RequestMessage> message) {
        handle(message.getPayload());
    }
    
}
