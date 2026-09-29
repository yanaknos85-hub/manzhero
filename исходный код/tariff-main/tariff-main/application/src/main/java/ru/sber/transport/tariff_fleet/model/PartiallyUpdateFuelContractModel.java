package ru.sber.transport.tariff_fleet.model;

import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointDto;

import java.util.List;
import java.util.Optional;

public record PartiallyUpdateFuelContractModel(
        Optional<Long> amountWithVatOpt,
        Optional<Long> amountWithoutVatOpt,
        Optional<String> logoOpt,
        Optional<List<ServicePointDto>> servicePointsOpt,
        Optional<String> servicePointsNameOpt
) {
}
