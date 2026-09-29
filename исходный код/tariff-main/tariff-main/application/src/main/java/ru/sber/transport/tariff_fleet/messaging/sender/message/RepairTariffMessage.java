package ru.sber.transport.tariff_fleet.messaging.sender.message;

import ru.sber.transport.messaging.Message;

import java.util.UUID;

public record RepairTariffMessage(
        UUID id,
        UUID contractId,
        boolean isFieldService,
        int hourNormalizedPrice,
        int detailDiscountPrice,
        int workWarranty,
        int mileageWarranty,
        int detailWarranty,
        boolean active,
        UUID departmentId
) implements Message<UUID> {

    @Override
    public UUID getId() {
        return id;
    }
}
