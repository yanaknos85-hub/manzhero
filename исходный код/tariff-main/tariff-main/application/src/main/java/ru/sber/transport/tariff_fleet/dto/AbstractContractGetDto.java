package ru.sber.transport.tariff_fleet.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.sberbank.ditsib.converters.LocalDateDeserializer;
import ru.sberbank.ditsib.converters.LocalDateSerializer;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Абстрактный договор (список)
 */
@Getter
@AllArgsConstructor
@Schema(name = "AbstractContractGetDto", description = "Абстрактный договор (список)")
public abstract class AbstractContractGetDto {
    @Schema(description = "Идентификатор записи", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private UUID id;
    @Schema(description = "Номер", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 50)
    private String number;
    @Schema(description = "Начало периода",
            requiredMode = Schema.RequiredMode.REQUIRED,
            type = "integer", format = "int64", example = "1696616506000")
    @NotNull
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate start;
    @Schema(description = "Окончание периода",
            requiredMode = Schema.RequiredMode.REQUIRED,
            type = "integer", format = "int64", example = "1696616506000")
    @NotNull
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate end;
    @Schema(description = "Статус договора", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean active;
}
