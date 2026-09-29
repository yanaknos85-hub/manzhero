package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.*;
import io.swagger.v3.oas.annotations.media.*;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.corpclient.serde.*;

import jakarta.validation.constraints.*;
import java.util.*;

/// Новые данные сотрудника
///
/// @param firstName Имя
 /// @param lastName Фамилия
 /// @param patronymic Отчество
 /// @param personnelNumber Табельный номер
 /// @param positionId идентификатор должности
 /// @param attributes атрибуты сотрудника
 /// @param email адрес электронной почты
 /// @param roles роли сотрудника
 /// @param status статус сотрудника
 /// @param mobilePhone номер телефона
 /// @param supervisorId идентификатор руководителя
@Schema(title = "Новые данные о сотруднике", description = "Данные о сотруднике")
public record NewEmployeeDTO(

        @NotBlank
        @Size(max = 20)
        @Schema(description = "Имя", maxLength = 20)
        String firstName,

        @NotBlank
        @Size(max = 30)
        @Schema(description = "Фамилия", maxLength = 30)
        String lastName,

        @Size(max = 30)
        @Schema(description = "Отчество", maxLength = 30)
        String patronymic,

        @NotBlank
        @Size(max = 10)
        @Schema(description = "Табельный номер", maxLength = 10)
        String personnelNumber,

        @NotNull
        @Schema(description = "Идентификатор должности")
        UUID positionId,

        @JsonDeserialize(using = PhoneNumberCorrectingDeserializer.class)
        @Pattern(regexp = "((\\+7|8)\\d{10})|(^$)")
        @Schema(description = "Мобильный номер")
        String mobilePhone,

        @Email(regexp = "^[a-zA-Z0-9_%.+\\-]+(\\.[a-zA-Z0-9_%.+\\-]+)*@[a-zA-ZА-ЯЁа-яё0-9.-]+\\.[a-zA-ZА-ЯЁа-яё]{2,6}$")
        @Schema(title = "E-Mail", pattern = "^[a-zA-Z0-9_%.+\\-]+(\\.[a-zA-Z0-9_%.+\\-]+)*@[a-zA-ZА-ЯЁа-яё0-9.-]+\\.[a-zA-ZА-ЯЁа-яё]{2,6}$")
        String email,

        @Schema(description = "Идентификатор руководителя")
        UUID supervisorId,

        @Schema(description = "Роли")
        Set<String> roles,

        @Schema(description = "Аттрибуты")
        Set<AttributeDto> attributes,

        @JsonAlias("status")
        @Schema(description = "Статус активности")
        ActiveStatus status
) {
}
