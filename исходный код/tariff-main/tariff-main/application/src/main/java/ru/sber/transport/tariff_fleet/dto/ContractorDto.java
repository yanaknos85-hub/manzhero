package ru.sber.transport.tariff_fleet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO для контрагента
 *
 * @param id идентификатор
 * @param name наименование
 */
@Schema(name = "ContractorDto", description = "Информация о контрагенте")
public record ContractorDto(
        
        @NotNull
        @Schema(description = "Идентификатор контрагента",
                requiredMode = Schema.RequiredMode.REQUIRED,
                type = "string",
                format = "uuid",
                example = "97ac91e3-82cc-4c90-9920-37517e3dae1d",
                minLength = 36,
                maxLength = 36)
        UUID id,
        @NotBlank
        @Schema(description = "Наименование контрагента",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 255,
                example = "0002_AFT-LIMITS-ORGANISATION")
        String name,
        @NotBlank
        @Schema(description = "Способ интеграции контрагента",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                maxLength = 100,
                example = "AUTOSERVICE")
        String integrationType
) {
}
