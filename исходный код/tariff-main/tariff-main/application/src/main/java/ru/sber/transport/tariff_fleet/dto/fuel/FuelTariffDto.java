package ru.sber.transport.tariff_fleet.dto.fuel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.sber.transport.tariff_fleet.dto.AbstractTariffResponse;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class FuelTariffDto extends AbstractTariffResponse {
    @Schema(description = "Подразделение корпоративного клиента в тарифе",
            requiredMode = Schema.RequiredMode.REQUIRED)
    protected String departmentName;
    @Schema(description = "Скидка от розничной сети",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1", maximum = "100")
    private BigDecimal discount;
}
