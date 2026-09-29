package ru.sber.transport.tariff_fleet.messaging.sender.message;

import ru.sber.transport.messaging.Message;

import java.util.UUID;

public record FuelTariffMessage(
        UUID tariffId,
        UUID contractId,
        UUID departmentId,
        boolean active,
        double discount,
        String humanReadableId
) implements Message<UUID> {
    
    @Override
    public UUID getId() {
        return tariffId;
    }
}
