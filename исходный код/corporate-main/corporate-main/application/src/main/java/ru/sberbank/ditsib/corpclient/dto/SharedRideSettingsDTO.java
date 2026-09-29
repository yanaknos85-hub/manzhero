package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.validation.annotation.Range;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Общая информация о настройках совместных поездок (родительский класс)
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Schema(
        title = "Общая информация о настройках совместных поездок",
        description = "Общая информация о настройках совместных поездок"
)
@Range(from = "economyIndicationYellowRangeLowerBorder", to = "economyIndicationYellowRangeUpperBorder")
public class SharedRideSettingsDTO {
    
    /**
     * Тип транспорта
     */
    @Schema(description = "Тип транспорта")
    @NotBlank
    private String transportType;
    
    /**
     * Идентификатор организации
     */
    @Schema(description = "Идентификатор организации")
    @NotNull
    private UUID organizationId;
    
    /**
     * Нижняя граница Желтого диапазона индикации экономии
     */
    @Schema(
            description = "Нижняя граница Желтого диапазона индикации экономии",
            minimum = "0",
            maximum = "Значение поля economyIndicationYellowRangeUpperBorder"
    )
    @NotNull @Min(0)
    private Integer economyIndicationYellowRangeLowerBorder;
    
    /**
     * Верхняя граница Желтого диапазона индикации экономии
     */
    @Schema(
            description = "Верхняя граница Желтого диапазона индикации экономии",
            minimum = "Значение поля economyIndicationYellowRangeLowerBorder",
            maximum = "100"
    )
    @NotNull @Max(100)
    private Integer economyIndicationYellowRangeUpperBorder;
}
