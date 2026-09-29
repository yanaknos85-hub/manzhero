package ru.sber.transport.tariff_fleet.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbContractPatchDto;

/**
 * Абстрактный договор (создание)
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "documentType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EwbContractPatchDto.class, name = "EWB")
})
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AbstractContractPatchDto", description = "Абстрактный договор (редактирование)")
public abstract class AbstractContractPatchDto {
    @Schema(description = "Тип документа", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private DocumentType documentType;
    @Schema(description = "Номер договора УВХД",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            minimum = "1", maximum = "50",
            example = "4387345-1")
    @Size(min = 1, max = 50)
    private String uvhd;
}
