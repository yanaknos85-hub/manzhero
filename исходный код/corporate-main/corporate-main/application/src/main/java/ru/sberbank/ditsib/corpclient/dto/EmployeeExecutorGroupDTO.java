package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Data transfer object with data about existing employee.
 */
@Getter
@Setter
@Schema(title = "Информация о сотруднике для групп исполнителей", description = "Данные сотрудника")
public class EmployeeExecutorGroupDTO {

    /**
     * Identifier
     */
    @NotNull
    @Schema(description = "Идентификатор")
    private UUID id;

    /**
     * FIO
     */
    @Schema(description = "ФИО сотрудника")
    private String name;

    /**
     * Unique personnel number
     */
    @Schema(description = "Табельный номер")
    private String personnelNumber;

    /**
     * Employee status
     */
    @Schema(description = "Статус сотрудника")
    EmployeeStatus status;
}
