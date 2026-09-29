package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * Объект обмена данными о исполнителе.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@Schema(title = "Новые данные о исполнителе", description = "Данные исполнителя")
public class ExecutorDTO {

    /**
     * Идентификатор сотрудника.
     */
    @NotBlank
    @Schema(description = "Идентификатор сотрудника")
    private UUID employeeId;

    /**
     * ФИО.
     */
    @Schema(description = "ФИО")
    private String employeeName;

    /**
     * Табельный номер.
     */
    @Schema(description = "Табельный номер")
    private String employeePersonnelNumber;

    /**
     * Статус.
     */
    @Schema(description = "Статус")
    private String employeeStatus;

    /**
     * Идентификатор подразделения.
     */
    @Schema(description = "Идентификатор подразделения")
    private UUID departmentId;

    /**
     * Наименование.
     */
    @Schema(description = "Наименование")
    private String departmentName;

    /**
     * Человеко-читаемый идентификатор.
     */
    @Schema(description = "Человеко-читаемый идентификатор")
    private String departmentHumanReadableId;

    /**
     * Статус.
     */
    @Schema(description = "Статус")
    private String departmentStatus;
}
