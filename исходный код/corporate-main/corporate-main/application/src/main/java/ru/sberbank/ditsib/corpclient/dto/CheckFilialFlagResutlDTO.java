package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * DTO for adding request.
 */
@Data
@AllArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Подразделение", description = "Данные Подразделения")
public class CheckFilialFlagResutlDTO {
    
    /**
     * Organization
     */
    @Schema(description = "Всего подразделений с флагом филиала")
    @Builder.Default
    private int numOfFilialFlags = 0;
    
    /**
     * Year
     */
    @Schema(description = "Снято флагов филиала")
    @Builder.Default
    private int numOfDeletedFlags = 0;
}
