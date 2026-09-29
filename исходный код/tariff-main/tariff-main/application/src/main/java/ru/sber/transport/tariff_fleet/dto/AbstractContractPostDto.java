package ru.sber.transport.tariff_fleet.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbContractPostDto;

/**
 * Абстрактный договор (создание)
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "documentType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EwbContractPostDto.class, name = "EWB")
})
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AbstractContractPostDto", description = "Абстрактный договор (создание)")
public abstract class AbstractContractPostDto {
    @Schema(description = "Тип документа", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private DocumentType documentType;
    @Schema(description = "Номер договора",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1", maximum = "50",
            example = "4387345-1")
    @NotBlank
    @Size(min = 1, max = 50)
    private String number;
    @Schema(description = "Номер договора УВХД",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            minimum = "1", maximum = "50",
            example = "4387345-1")
    @Size(min = 1, max = 50)
    private String uvhd;
    @Schema(description = "Период действия договора", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull
    private DateRange period;
}
