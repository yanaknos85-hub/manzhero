package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Элемент Настроек контрольных сроков Лимита для организации
 */
@Setter
@Getter
@Schema(
        title = "Данные элемента Настроек контрольных сроков Лимита для организации",
        description = "Данные элемента Настроек контрольных сроков Лимита"
)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LimitDeadlineSettingsItemDTO extends NewLimitDeadlineSettingsItemDTO {
    
    /** ID элемента настройки */
    @NotNull
    @Schema(description = "ID элемента настройки")
    private UUID id;
}
