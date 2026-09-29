package ru.sberbank.ditsib.transport.approvals.messaging.listeners;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.approvals.messaging.message.TariffMessage;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Слушатель сообщений о тарифах
 */
public interface TariffListener extends Consumer<Message<TariffMessage>> {
    
    /**
     * Handler сообщений о тарифах
     * @param message сообщение
     * @param key message key
     */
    void handle(UUID key, TariffMessage message);

    default void accept(Message<TariffMessage> tariffMessage) {
        final var id = tariffMessage.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class);
        final var payload = tariffMessage.getPayload();
        handle(id, payload);
    }
}
