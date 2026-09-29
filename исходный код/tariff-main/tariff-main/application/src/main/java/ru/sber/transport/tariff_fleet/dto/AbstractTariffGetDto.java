package ru.sber.transport.tariff_fleet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * Абстрактный тариф (список)
 */
@Getter
@AllArgsConstructor
@Schema(name = "AbstractTariffGetDto", description = "Абстрактный тариф (список)")
public abstract class AbstractTariffGetDto {
    @Schema(description = "Идентификатор записи", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private UUID id;
    @Schema(description = "Номер договора", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 50)
    private String contractNumber;
    @Schema(description = "Человекочитаемый идентификатор",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maximum = "17",
            example = "TF-0008-00000002")
    @NotBlank
    @Size(max = 17)
    private String humanReadableId;
    @Schema(description = "Статус договора", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean active;
}
