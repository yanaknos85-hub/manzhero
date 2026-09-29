package ru.sber.transport.tariff_fleet.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.dto.fuel.FuelContractPostAllOrganizationsDto;
import ru.sber.transport.tariff_fleet.dto.repair.RepairContractPostAllOrganizationsDto;
import ru.sberbank.ditsib.converters.LocalDateDeserializer;
import ru.sberbank.ditsib.converters.LocalDateSerializer;

import java.time.LocalDate;

/**
 * Абстрактный договор (создание)
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "documentType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RepairContractPostAllOrganizationsDto.class, name = "REPAIR_AND_MAINTENANCE"),
        @JsonSubTypes.Type(value = FuelContractPostAllOrganizationsDto.class, name = "FUEL"),
})
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AbstractContractPostAllOrganizationsDto", description = "Абстрактный договор создание (все организации)")
public abstract class AbstractContractPostAllOrganizationsDto {
    @Schema(description = "Тип документа",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "FUEL")
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
    @Schema(description = "Начало периода",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2023-11-16")
    @NotNull
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate start;
    @Schema(description = "Окончание периода",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2023-11-16")
    @NotNull
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate end;
}
