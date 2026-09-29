package ru.sber.transport.tariff_fleet.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.sber.transport.tariff_fleet.annotation.ContractType;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.dto.AbstractTariffPostDto;

import java.util.UUID;

@AllArgsConstructor
@ContractType(DocumentType.EWB)
@EqualsAndHashCode(callSuper = true)
@Getter
@ToString
@Schema(name = "EwbTariffPostDto", description = "Тариф ЭПЛ")
public class EwbTariffPostDto extends AbstractTariffPostDto {
    @NotNull
    @Schema(description = "Идентификатор записи о подразделении контрагента", requiredMode = Schema.RequiredMode.REQUIRED)
    private final UUID departmentId;
    @Schema(description = "Стоимость осмотра",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0", maximum = "999999",
            example = "344444")
    @NotNull
    @Max(value = 999999L)
    @PositiveOrZero
    private final Long amount;
}