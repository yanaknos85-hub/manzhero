package ru.sber.transport.tariff_fleet.messaging.sender.message;

import ru.sber.transport.messaging.Message;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RepairContractMessage(
        UUID id,
        UUID organizationId,
        String number,
        String uvhd,
        LocalDate start,
        LocalDate end,
        boolean active,
        String servicePointsName,
        Long amountWithoutVat,
        Long amountWithVat,
        UUID contractorId,
        List<ServicePoint> servicePoints
) implements Message<UUID> {

    @Override
    public UUID getId() {
        return id;
    }

    public record ServicePoint(
            UUID id,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            boolean active,
            UUID logoS3Id
    ) {}
}
