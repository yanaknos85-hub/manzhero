package ru.sber.transport.tariff_fleet.dto.fuel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sber.transport.tariff_fleet.database.model.Contract_;

/**
 * Настройка сортировки
 *
 * @param property Тип сортировки {@link FuelContractSortOption}
 * @param directionAsc Направление сортировки
 */
@Schema(title = "Настройка сортировки", description = "Настройка сортировки")
public record FuelContractSortSetting(
        @Schema(description = "Тип сортировки", example = "NUMBER")
        @NotNull
        FuelContractSortSetting.FuelContractSortOption property,
        @Schema(description = "Направление сортировки", example = "true")
        @NotNull
        boolean directionAsc
) {
    
    /**
     * Типы сортировок
     */
    @Getter
    @RequiredArgsConstructor
    public enum FuelContractSortOption {
        NUMBER(Contract_.NUMBER),
        CONTRACTOR_NAME("contractorName");
        private final String fieldName;
    }
    
}