package ru.sber.transport.tariff_fleet.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Настройка сортировки
 *
 * @param property Тип сортировки {@link EwbContractSortOption}
 * @param directionAsc Направление сортировки
 */
@Schema(title = "Настройка сортировки", description = "Настройка сортировки")
public record EwbContractSortSetting(
        @Schema(description = "Тип сортировки", example = "NUMBER")
        @NotNull
        EwbContractSortSetting.EwbContractSortOption property,
        @Schema(description = "Направление сортировки", example = "true")
        @NotNull
        boolean directionAsc
) {
    
    /**
     * Типы сортировок
     */
    @Getter
    @RequiredArgsConstructor
    public enum EwbContractSortOption {
        NUMBER("Номер"),
        CONTRACTOR_ORGANIZATION_NAME("Наименование организации контрагента");
        private final String description;
    }
    
}