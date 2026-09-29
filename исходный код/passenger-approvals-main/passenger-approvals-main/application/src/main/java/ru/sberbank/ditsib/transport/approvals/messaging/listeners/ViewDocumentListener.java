package ru.sberbank.ditsib.transport.approvals.messaging.listeners;

import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.approvals.messaging.message.ViewDocumentMessage;

import java.util.function.Consumer;

/**
 * Слушатель просмотра документа
 */
public interface ViewDocumentListener extends Consumer<Message<ViewDocumentMessage>> {
    /**
     * Получено новое сообщение.
     *
     * @param message сообщение.
     */
    void handle(ViewDocumentMessage message);

    default void accept(Message<ViewDocumentMessage> message) {
        handle(message.getPayload());
    }
}
