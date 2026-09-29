package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.time.temporal.ChronoUnit;

/**
 * Новый элемент Настроек контрольных сроков Лимита для организации
 */
@Setter
@Getter
@Schema(
        title = "Новые данные элемента Настроек контрольных сроков Лимита для организации",
        description = "Новые данные элемента Настроек контрольных сроков Лимита"
)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class NewLimitDeadlineSettingsItemDTO {
    
    /** Единица измерения времени */
    @NotNull
    @Schema(description = "Единица измерения времени")
    private ChronoUnit unit;
    
    /** Значение контрольного срока (в указанных ед.изменрения) */
    @NotNull
    @Schema(
            description = "Значение контрольного срока (в указанных ед.изменрения)",
            minimum = "0", maximum = "999"
    )
    private Integer value;
}
