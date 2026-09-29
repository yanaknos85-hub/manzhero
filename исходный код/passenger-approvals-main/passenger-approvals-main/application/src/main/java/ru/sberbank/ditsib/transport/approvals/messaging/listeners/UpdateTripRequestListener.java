package ru.sberbank.ditsib.transport.approvals.messaging.listeners;

import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.approvals.messaging.message.UpdateTripRequestMessage;

import java.util.function.Consumer;

/**
 * Слушатель заявок на изменение поездки.
 */
public interface UpdateTripRequestListener extends Consumer<Message<UpdateTripRequestMessage>> {
    /**
     * Получено новое сообщение.
     *
     * @param message сообщение.
     */
    void handle(UpdateTripRequestMessage message);

    default void accept(Message<UpdateTripRequestMessage> message) {
        handle(message.getPayload());
    }
    
}
