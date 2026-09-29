package ru.sberbank.ditsib.transport.limits.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.limits.constants.SettingsNames;

/**
 * DTO for adding request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Настройки лимитов", description = "Настройки лимитов")
public class LimitSettingsDTO {
    
    /**
     * Year
     */
    @Schema(description = "Ключ")
    private SettingsNames name;
    
    /**
     * Status
     */
    @Schema(description = "Значение")
    private String value;
}
