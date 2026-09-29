package ru.sberbank.ditsib.transport.approvals.messaging.listeners;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import ru.sberbank.ditsib.transport.approvals.messaging.message.LimitMessage;

import java.util.function.Consumer;

/**
 * Слушатель событий лимита департамента.
 */
public interface DepLimitListener extends Consumer<Message<LimitMessage>> {
    
    /**
     * Получено новое сообщение.
     *
     * @param message сообщение.
     */
    void handle(@Payload LimitMessage message);

    @Override
    default void accept(Message<LimitMessage> limitMessageMessage) {
        handle(limitMessageMessage.getPayload());
    }
}
