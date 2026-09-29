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
@Schema(title = "Краткая информация о подразделении", description = "Данные подразделения")
public class DepartmentShortDTO {
    
    /**
     * Identifier
     */
    @NotNull
    @Schema(description = "Идентификатор")
    private UUID id;
    
    /**
     * Name of department
     */
    @Schema(description = "Название")
    private String departmentName;
    
    /**
     * Status of department.
     */
    @Schema(description = "Статус")
    private DepartmentStatus status;

    /**
     * Code of department.
     */
    @Schema(description = "Код подразделения")
    private String code;

    /**
     * Head of department.
     */
    @Schema(description = "Руководитель подразделения")
    private String headNameFirst;

    /**
     * Head of department.
     */
    @Schema(description = "Руководитель подразделения")
    private String headNameLast;

    /**
     * Organization.
     */
    @Schema(description = "Имя организации")
    private String organizationName;

    /**
     * Parent department.
     */
    @Schema(description = "Родительское подразделение")
    private String parentDepartmentName;

    /**
     * Location of department.
     */
    @Schema(description = "Территориальное местоположение")
    private String location;

    /**
     * EASUP ID.
     */
    @Schema(description = "EASUP ID")
    private String easupId;
}
