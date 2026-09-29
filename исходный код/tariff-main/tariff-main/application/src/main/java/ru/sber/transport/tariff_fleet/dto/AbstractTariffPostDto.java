package ru.sber.transport.tariff_fleet.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbTariffPostDto;

import java.util.UUID;

/**
 * Абстрактный тариф (создание)
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "documentType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EwbTariffPostDto.class, name = "EWB")
})
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AbstractTariffPostDto", description = "Абстрактный тариф (создание)")
public abstract class AbstractTariffPostDto {
    @Schema(description = "Тип документа", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private DocumentType documentType;
    
    @NotNull
    @Schema(description = "ID договора", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID contractId;
}
