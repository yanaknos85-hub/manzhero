package ru.sber.transport.tariff_fleet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * Абстрактный тариф (получение по ID)
 */
@Getter
@AllArgsConstructor
@Schema(name = "AbstractTariffGetByIdDto", description = "Абстрактный тариф (получение по ID)")
public class AbstractTariffGetByIdDto {
    
    @NotBlank
    @Schema(description = "Идентификатор тарифа",
            requiredMode = Schema.RequiredMode.REQUIRED,
            type = "string",
            format = "uuid",
            example = "310a35ba-b047-41a4-b17a-6afc65ad0d41",
            minLength = 36, maxLength = 36)
    private UUID id;
    
    @NotBlank
    @Size(min = 1, max = 50)
    @Schema(description = "Номер договора",
            requiredMode = Schema.RequiredMode.REQUIRED,
            type = "string",
            example = "001b5a57",
            minLength = 1, maxLength = 50)
    private String contractNumber;
}
