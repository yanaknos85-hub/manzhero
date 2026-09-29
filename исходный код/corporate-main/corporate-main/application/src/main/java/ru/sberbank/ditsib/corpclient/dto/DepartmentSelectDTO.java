package ru.sberbank.ditsib.corpclient.dto;

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
@Schema(title = "Информация о подразделении", description = "Данные подразделения")
public class DepartmentSelectDTO extends NewDepartmentDTO {
    
    /**
     * Root organization
     */
    @NotNull
    @Schema(description = "Идентификатор подразделения")
    private UUID organizationId;
    
    /**
     * Identifier
     */
    @NotNull
    @Schema(description = "Идентификатор")
    private UUID id;
    
    /**
     * Identifier (human readable)
     */
    @Schema(description = "Идентификатор (человекочитаемый)")
    private String humanReadableId;

    @Schema(description = "Уровень")
    private int level;
}
