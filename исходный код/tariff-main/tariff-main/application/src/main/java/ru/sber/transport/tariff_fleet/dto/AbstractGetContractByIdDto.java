package ru.sber.transport.tariff_fleet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * Абстрактный договор (получение по ID)
 */
@Getter
@AllArgsConstructor
@Schema(name = "AbstractGetContractByIdDto", description = "Абстрактный договор (получение по ID)")
public class AbstractGetContractByIdDto {
    @NotNull
    @Schema(description = "ID договора",
            requiredMode = Schema.RequiredMode.REQUIRED,
            type = "string",
            format = "uuid",
            minLength = 36, maxLength = 36,
            example = "95671880-4870-4111-876b-088207208668"
    )
    private UUID id;
    
    @NotBlank
    @Size(min = 1, max = 50)
    @Schema(description = "Номер",
            requiredMode = Schema.RequiredMode.REQUIRED,
            type = "string",
            minLength = 1, maxLength = 5,
            example = "4387345-1")
    private String number;
    
    @NotBlank
    @Size(min = 1, max = 50)
    @Schema(description = "Номер договора УВХД",
            requiredMode = Schema.RequiredMode.REQUIRED,
            type = "string",
            minLength = 1, maxLength = 5,
            example = "4387345-1")
    private String uvhd;
    
    @NotNull
    @Schema(description = "Период действия договора",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private DateRange period;
}
