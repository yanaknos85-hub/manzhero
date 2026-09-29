package ru.sber.transport.tariff_fleet.dto.repair;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sber.transport.tariff_fleet.database.model.Contract_;

/**
 * Настройка сортировки
 *
 * @param property Тип сортировки {@link RepairContractSortOption}
 * @param directionAsc Направление сортировки
 */
@Schema(title = "Настройка сортировки", description = "Настройка сортировки")
public record RepairContractSortSetting(
        @Schema(description = "Тип сортировки", example = "NUMBER")
        @NotNull
        RepairContractSortSetting.RepairContractSortOption property,
        @Schema(description = "Направление сортировки", example = "true")
        @NotNull
        boolean directionAsc
) {
    
    /**
     * Типы сортировок
     */
    @Getter
    @RequiredArgsConstructor
    public enum RepairContractSortOption {
        NUMBER(Contract_.NUMBER),
        CONTRACTOR_NAME("contractorName");
        private final String fieldName;
    }
    
}