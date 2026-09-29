package ru.sberbank.ditsib.transport.limits.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;

/**
 * DTO for adding request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Лимит", description = "Входные данные для создания лимита лимита")
public class DepLimitEditDTO {
    
    /**
     * Final sharing flag
     */
    @Schema(description = "Конечное распределение")
    private Boolean finalSharing;
    
    /**
     * Use my limit flag
     */
    @Schema(description = "Использовать лимит моего подразделения")
    private Boolean useThisLimit;
    
    /**
     * Status of limit
     */
    @Schema(description = "Статус лимита")
    private LimitStatus limitStatus;
}
