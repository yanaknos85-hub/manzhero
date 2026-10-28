package ru.sber.transport.trip.registry.messaging;

import ru.sberbank.ditsib.transport.messaging.Message;

import java.util.UUID;

/**
 * Сообщение регистра.
 *
 */
public record RegistryMessage(
        UUID registryId,
        String registryState,
        String actId,
        Integer lockingIndicator,
        Integer updStatus,
        String error,
        String serviceType
) implements Message<UUID> {

    @Override
    public UUID getId() {
        return registryId();
    }
}
