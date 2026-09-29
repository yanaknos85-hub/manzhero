package ru.sberbank.ditsib.transport.approvals.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(title = "Информация о сотруднике", description = "Данные сотрудника")
public record EmployeeDTO(

        @NotNull
        @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        @Schema(description = "Имя")
        String firstName,
        @Schema(description = "Фамилия")
        String lastName,
        @Schema(description = "Отчество")
        String patronymic,
        @Schema(description = "Табельный номер")
        String personnelNumber,
        @Schema(description = "Идентификатор подразделения")
        UUID departmentId,
        @Schema(description = "Идентификатор пользователя")
        UUID userId
) {
}
