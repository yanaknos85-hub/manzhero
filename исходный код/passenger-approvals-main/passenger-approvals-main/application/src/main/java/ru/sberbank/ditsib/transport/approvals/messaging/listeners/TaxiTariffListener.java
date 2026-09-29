package ru.sberbank.ditsib.transport.approvals.messaging.listeners;

import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.approvals.messaging.message.TaxiTariffMessage;

import java.util.UUID;
import java.util.function.Consumer;

public interface TaxiTariffListener extends Consumer<Message<TaxiTariffMessage>> {
    
    /**
     * Обработка сообщений тарифов для такси.
     *
     * @param key message key
     * @param message message.
     */
    void handle(UUID key, TaxiTariffMessage message);

    default void accept(Message<TaxiTariffMessage> message) {
        final var payload = message.getPayload();
        handle(payload.getId(), payload);
    }
}
