package ru.sberbank.ditsib.transport.approvals.messaging.listeners;

import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.approvals.messaging.message.DelegateMessage;

import java.util.function.Consumer;

/**
 * Слушатель событий делегатов.
 */
public interface DelegateListener extends Consumer<Message<DelegateMessage>> {

    @Override
    default void accept(Message<DelegateMessage> delegateMessageMessage) {
        handle(delegateMessageMessage.getPayload());
    }

    void handle(DelegateMessage delegateMessage);
}
