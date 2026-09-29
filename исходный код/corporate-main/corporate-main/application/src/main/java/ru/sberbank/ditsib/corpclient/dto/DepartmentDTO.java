package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Data transfer object with data about existing department.
 */
@Getter
@Setter
@JsonPropertyOrder({ "id" })
@Schema(title = "Информация о подразделении", description = "Данные подразделения")
public class DepartmentDTO extends DepartmentSelectDTO {
    
    /**
     * Children collection
     */
    @Schema(description = "Дочерние подразделения")
    private Set<DepartmentShortDTO> children = new HashSet<>();
    
    /**
     * Employees collection
     */
    @Schema(description = "Сортудники")
    private Set<EmployeeShortDTO> employees = new HashSet<>();
    
    /**
     * Full structure path
     */
    @Schema(description = "Структурный путь подразделения")
    private String fullStructurePath;
}
