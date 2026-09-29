package ru.sberbank.ditsib.corpclient.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Size;
import java.util.UUID;

@Getter
@Setter
@Schema(title = "Данные родительского подразделения", description = "Данные родительского подразделения")
public class DepartmentParentDTO {

    @Schema(description = "Идентификатор")
    private UUID id;
    
    /**
     * Name of department
     */
    @Size(min = 1, max = 255)
    @Schema(description = "Название подразделения")
    private String departmentName;
}
