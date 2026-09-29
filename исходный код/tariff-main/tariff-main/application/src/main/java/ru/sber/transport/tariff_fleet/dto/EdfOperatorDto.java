package ru.sber.transport.tariff_fleet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Дто Оператор ЭДО
 */
@Schema(name = "EdfOperatorDto", description = "Оператор ЭДО")
public record EdfOperatorDto(
        @Schema(description = "Идентификатор оператора ЭДО",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "1", maximum = "10",
                example = "2BM")
        @NotBlank
        @Size(min = 1, max = 10)
        String id,
        @Schema(description = "Отображаемое наименование",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "1", maximum = "50",
                example = "2AE — «Калуга-Астрал»")
        @NotBlank
        @Size(min = 1, max = 50)
        String title
) {
}