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
import ru.sber.transport.tariff_fleet.dto.AbstractTariffPatchDto;

@AllArgsConstructor
@ContractType(DocumentType.EWB)
@EqualsAndHashCode(callSuper = true)
@Getter
@ToString
@Schema(name = "EwbTariffPatchDto", description = "Тариф ЭПЛ")
public class EwbTariffPatchDto extends AbstractTariffPatchDto {
    
    @NotNull
    @Max(value = 999999L)
    @PositiveOrZero
    @Schema(description = "Стоимость осмотра",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0", maximum = "999999",
            example = "344444")
    private final Long amount;
}