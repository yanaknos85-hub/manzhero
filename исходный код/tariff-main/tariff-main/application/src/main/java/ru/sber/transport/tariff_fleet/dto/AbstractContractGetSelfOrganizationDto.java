package ru.sber.transport.tariff_fleet.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sberbank.ditsib.converters.LocalDateDeserializer;
import ru.sberbank.ditsib.converters.LocalDateSerializer;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Абстрактный договор (список)
 */
@Getter
@AllArgsConstructor
@Schema(name = "AbstractContractGetSelfOrganizationDto", description = "Абстрактный договор (список среди всех организаций)")
public abstract class AbstractContractGetSelfOrganizationDto {
    @Schema(description = "Идентификатор записи", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private UUID id;
    @Schema(description = "Тип документа",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "REPAIR_AND_MAINTENANCE")
    @NotNull
    private DocumentType documentType;
    @Schema(description = "Номер", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 50)
    private String number;
    @Schema(description = "Начало периода",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "2023-11-16")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate start;
    @Schema(description = "Окончание периода",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "2023-11-16")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate end;
    @Schema(description = "Статус договора", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean active;
}
