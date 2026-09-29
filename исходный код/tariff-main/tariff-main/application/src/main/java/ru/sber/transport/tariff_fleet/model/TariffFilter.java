package ru.sber.transport.tariff_fleet.model;

import lombok.Builder;

import java.util.UUID;

@Builder
public record TariffFilter(
        UUID contractorId,
        UUID contractId,
        UUID organizationId,
        String humanReadableId,
        Boolean active
) {
}
