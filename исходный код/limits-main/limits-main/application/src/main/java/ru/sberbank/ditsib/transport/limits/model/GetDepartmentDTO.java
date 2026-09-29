package ru.sberbank.ditsib.transport.limits.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Data transfer object with data about existing department.
 */
@Getter
@Setter
@JsonPropertyOrder({ "id" })
@Schema(title = "Краткая информация о подразделении", description = "Данные подразделения")
public class GetDepartmentDTO {
    
    /**
     * Identifier
     */
    @NotNull
    @Schema(description = "Идентификатор")
    private UUID id;
    
    /**
     * Name of department
     */
    @Schema(description = "Код")
    private String code;
    
    /**
     * Name of department
     */
    @Schema(description = "Название")
    private String departmentName;
    
    /**
     * Человекочитаемый идентификатор.
     */
    @Schema(description = "Человекочитаемый идентификатор")
    private String humanReadableId;
    
    /**
     * Рук-ль подразделения.
     */
    @Schema(description = "Рук-ль подразделения")
    private GetEmployeeDTO departmentHead;
}