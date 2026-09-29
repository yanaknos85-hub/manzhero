package ru.sberbank.ditsib.transport.approvals.messaging.listeners;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import ru.sberbank.ditsib.transport.approvals.messaging.message.TripPurposeMessage;

import java.util.function.Consumer;

/**
 * Слушатель создания/удаления целей
 */
public interface TripPurposeListener extends Consumer<Message<TripPurposeMessage>> {
    /**
     * Получено новое сообщение.
     *
     * @param message сообщение.
     */
    void handle(@Payload TripPurposeMessage message);

    default void accept(Message<TripPurposeMessage> message) {
        handle(message.getPayload());
    }
}
