package ru.sber.transport.tariff_fleet.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Настройка сортировки
 *
 * @param property Тип сортировки {@link EwbTariffSortOption}
 * @param directionAsc Направление сортировки
 */
@Schema(title = "Настройка сортировки", description = "Настройка сортировки")
public record EwbTariffSortSetting(
        @Schema(description = "Тип сортировки", example = "NUMBER")
        @NotNull
        EwbTariffSortSetting.EwbTariffSortOption property,
        @Schema(description = "Направление сортировки", example = "true")
        @NotNull
        boolean directionAsc
) {
    
    /**
     * Типы сортировок
     */
    @Getter
    @RequiredArgsConstructor
    public enum EwbTariffSortOption {
        HUMAN_READABLE_ID("Человекочитаемый идентификатор");
        private final String description;
    }
    
    /**
     * Конструктор для определения направления сортировки по-умолчанию
     */
    private EwbTariffSortSetting() {
        this(EwbTariffSortOption.HUMAN_READABLE_ID, true);
    }
}