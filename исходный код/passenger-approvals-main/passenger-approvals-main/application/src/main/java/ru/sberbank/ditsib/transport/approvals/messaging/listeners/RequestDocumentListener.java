package ru.sberbank.ditsib.transport.approvals.messaging.listeners;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestDocumentMessage;

import java.util.function.Consumer;

/**
 * Слушатель создания/удаления документа
 */
public interface RequestDocumentListener extends Consumer<Message<RequestDocumentMessage>> {
    /**
     * Получено новое сообщение.
     *
     * @param message сообщение.
     */
    void handle(@Payload RequestDocumentMessage message);

    default void accept(Message<RequestDocumentMessage> message) {
        handle(message.getPayload());
    }
}
