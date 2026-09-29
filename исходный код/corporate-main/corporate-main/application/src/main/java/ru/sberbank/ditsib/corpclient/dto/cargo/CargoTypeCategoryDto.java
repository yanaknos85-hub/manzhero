package ru.sberbank.ditsib.corpclient.dto.cargo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Type and category of cargo.
 */
@Builder
@Getter
@Schema(title = "Виды и категории грузов", description = "Виды и категории грузов")
public class CargoTypeCategoryDto {
    /**
     * Идентивикатор
     */
    @Schema(description = "Идентификатор")
    private final UUID id;
    
    /**
     * Название
     */
    @Schema(description = "Название")
    private final String name;
    
    /**
     * Значение для интерфейса
     */
    @Schema(description = "Значение для интерфейса")
    private final String value;

    /**
     * Единица измерения
     */
    @Schema(description = "Единица измерения")
    private final String unit;
}
