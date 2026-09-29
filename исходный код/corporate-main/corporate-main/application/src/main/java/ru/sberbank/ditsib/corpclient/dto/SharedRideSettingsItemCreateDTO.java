package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.Valid;
import java.util.Set;

/**
 * Информация о новых настройках совместных поездок. Не содержит ID
 */
@Getter
@Setter
@Schema(
        title = "Информация о новом элементе настройки совместных поездок",
        description = "Информация о новом элементе настройки совместных поездок"
)
@Builder(builderMethodName = "parentBuilder")
@AllArgsConstructor
public class SharedRideSettingsItemCreateDTO {
    
    /**
     * Должности для настроек совместных поездок
     */
    @Schema(description = "Должности для настроек совместных поездок")
    private Set<@Valid SettingsPositionDTO> positions;
    
    /**
     * Признаки сотрудника для настроек совместных поездок
     */
    @Schema(description = "Признаки сотрудника для настроек совместных поездок")
    private Set<@Valid SettingsAttributeDTO> attributes;
    
    /**
     * Сотрудники для настроек совместных поездок
     */
    @Schema(description = "Сотрудники для настроек совместных поездок")
    private Set<@Valid SettingsEmployeeDTO> employees;
}
