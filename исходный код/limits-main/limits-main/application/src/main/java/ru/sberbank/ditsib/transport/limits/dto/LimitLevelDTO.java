package ru.sberbank.ditsib.transport.limits.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;

/**
 * DTO for adding request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Лимит", description = "Данные лимита")
public class LimitLevelDTO {
    
    /**
     * Organization
     */
    @Schema(description = "Лимит")
    private Limit limit;
    
    /**
     * Year
     */
    @Schema(description = "Уровень")
    private Integer level;
}
