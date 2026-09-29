package ru.sber.transport.tariff_fleet.dto.fuel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.sber.transport.tariff_fleet.annotation.ContractType;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.dto.AbstractCreateTariffRequest;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@ContractType(DocumentType.FUEL)
@EqualsAndHashCode(callSuper = true)
@Getter
@ToString
@Schema(name = "CreateFuelTariffDto", description = "Абстрактный запрос на создание тарифа для договора заправки топливом")
public class CreateFuelTariffDto extends AbstractCreateTariffRequest {

    @Schema(description = "ID департамента",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "f173f387-f283-466c-9124-89acc1678ac3")
    @NotNull
    private UUID departmentId;

    @Schema(description = "Скидка от розничной сети",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0", maximum = "100")
    @NotNull
    @Min(0)
    @Max(100)
    private BigDecimal discount;
}
