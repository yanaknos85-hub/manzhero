package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Data transfer object with data about existing department.
 */
@Getter
@Setter
@Schema(title = "Информация о подразделении для групп исполнителей", description = "Данные подразделения")
public class DepartmentExecutorGroupDTO extends DepartmentShortDTO {

    /**
     * Identifier (human readable)
     */
    @Schema(description = "Идентификатор (человекочитаемый)")
    private String humanReadableId;
}
