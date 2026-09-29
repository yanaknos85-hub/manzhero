package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.*;

import jakarta.validation.constraints.*;
import java.util.*;

/// Объект сотрудника для селекта
///
/// @param firstName Имя сотрудника
/// @param supervisorId Идентификатор руководителя
 /// @param lastName Фамилия сотрудника
 /// @param patronymic Отчество сотрудника
 /// @param personnelNumber Табельный номер сотрудника
 /// @param positionId Идентификатор должности сотрудника
 /// @param mobilePhone Мобильный номер сотрудника
 /// @param email E-Mail сотрудника
 /// @param roles Список ролей
 /// @param id Идентификатор сотрудника
 /// @param userId Идентификатор пользователя
 /// @param humanReadableId Человекочитаемый идентификатор сотрудника
@Schema(title = "Информация о сотруднике", description = "Данные сотрудника")
public record EmployeeSelectDTO(

        @Schema(description = "Имя")
        String firstName,

        @Schema(description = "Фамилия")
        String lastName,

        @Schema(description = "Отчество")
        String patronymic,

        @Schema(description = "Табельный номер")
        String personnelNumber,

        @NotNull
        @Schema(description = "Идентификатор должности")
        UUID positionId,

        @Schema(description = "Мобильный номер")
        String mobilePhone,

        @Schema(description = "E-Mail")
        String email,

        @Schema(description = "Идентификатор руководителя")
        UUID supervisorId,

        @Schema(description = "Роли")
        Set<String> roles,

        @Schema(description = "Идентификатор")
        UUID id,

        @Schema(description = "Идентификатор пользователя")
        UUID userId,

        @Schema(description = "Идентификатор (человекочитаемый)")
        String humanReadableId
) implements HasEmployeeData {
}
