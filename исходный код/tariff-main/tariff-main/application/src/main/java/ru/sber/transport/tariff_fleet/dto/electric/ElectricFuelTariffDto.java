package ru.sber.transport.tariff_fleet.dto.electric;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sber.transport.tariff_fleet.dto.AbstractTariffResponse;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class ElectricFuelTariffDto extends AbstractTariffResponse {
    @Schema(description = "Парковка", requiredMode = Schema.RequiredMode.REQUIRED)
    private String parking;
}
