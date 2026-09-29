package ru.sber.transport.tariff_fleet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Настройка разделения на страницы
 *
 * @param page Номер страницы
 * @param size Количество элементов на странице
 */
@Schema(title = "Настройка пагинации", description = "Настройка пагинации")
public record PageSetting(
        @Schema(description = "Номер страницы", example = "0")
        @PositiveOrZero
        int page,
        @Schema(description = "Количество элементов на странице", example = "10")
        @Positive
        int size
) {
}