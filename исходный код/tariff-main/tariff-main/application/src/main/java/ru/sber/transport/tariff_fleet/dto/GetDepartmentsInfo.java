package ru.sber.transport.tariff_fleet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * ДТО для запроса списка подразделений для списка идентификаторов организаций
 *
 * @param organizationName Наименование организации
 * @param departmentList Список подразделений
 */
@Schema(name = "GetDepartmentsInfo", title = "Список подразделений",
        description = "Ответ для запроса списка подразделений для списка идентификаторов организаций")
public record GetDepartmentsInfo(
        
        @NotBlank(message = "Наименование организации не может быть пустым")
        @Schema(description = "Наименование организации",
                requiredMode = Schema.RequiredMode.REQUIRED,
                type = "string",
                title = "Наименование организации",
                example = "ООО \"Сбербанк\"",
                maxLength = 255)
        String organizationName,
        
        @Schema(name = "departmentList", title = "Список подразделений")
        List<DepartmentInfoDto> departmentList
) {
}
