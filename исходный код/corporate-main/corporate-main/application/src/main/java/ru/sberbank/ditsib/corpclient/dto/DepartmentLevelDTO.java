package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.corpclient.database.model.Department;

/**
 * DTO for adding request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Подразделение", description = "Данные Подразделения")
public class DepartmentLevelDTO {
    
    /**
     * Organization
     */
    @Schema(description = "Подразделение")
    private Department department;
    
    /**
     * Year
     */
    @Schema(description = "Уровень")
    private Integer level;
}
