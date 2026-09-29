package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.*;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.transport.constants.*;

import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;


/// Объект обмена данными о сотруднике
///
/// @param firstName Имя
 /// @param lastName Фамилия
 /// @param patronymic Отчество
 /// @param personnelNumber Табельный номер
 /// @param positionId Идентификатор должности
 /// @param mobilePhone Мобильный номер
 /// @param email E-Mail
 /// @param supervisorId Идентификатор руководителя
 /// @param roles Список ролей
 /// @param id Идентификатор
 /// @param userId Идентификатор пользователя
 /// @param humanReadableId Человекочитаемый идентификатор
 /// @param availableTransportTypes Типы транспорта
 /// @param personalCars Тип сотрудника
 /// @param organizationId Идентификатор организации
 /// @param status Статус
 /// @param gender Пол
 /// @param fireDate Дата увольнения
 /// @param externalEmail Электронная почта
 /// @param room Номер комнаты
 /// @param consent Подписание ПДн
 /// @param attributes Список атрибутов
 /// @param positionName Название должности
 /// @param departmentId Идентификатор подразделения
 /// @param orgStructureType Тип организации
 /// @param organizationName Название организации
 /// @param departmentName Название подразделения
@Schema(title = "Информация о сотруднике", description = "Данные сотрудника")
public record EmployeeDTO(

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
        String humanReadableId,
        @Schema(description = "Доступные типы транспорта")
        Set<TransportTypeEnum> availableTransportTypes,

        @Schema(description = "Личные автомобили")
        Set<PersonalCarDTO> personalCars,

        @Deprecated(since = "2023-03-15")
        @Schema(description = "Идентификатор организации", deprecated = true)
        UUID organizationId,

        @Schema(description = "Статус сотрудника")
        EmployeeStatus status,

        @Schema(description = "Пол сотрудника")
        Gender gender,

        @Schema(description = "Дата увольнения")
        LocalDate fireDate,

        @Schema(description = "Внешний E-Mail")
        String externalEmail,

        @Schema(description = "Комната")
        String room,

        @Schema(description = "Признак наличия согласия")
        boolean consent,

        @Schema(description = "Список признаков сотрудника")
        Set<AttributeDto> attributes,

        @Schema(description = "Название должности")
        String positionName,

        @Deprecated(since = "2023-03-15")
        @NotNull
        @Schema(description = "Идентификатор подразделения", deprecated = true)
        UUID departmentId,

        @Schema(description = "Тип сотрудника")
        OrgStructureType orgStructureType,

        @Schema(description = "Название организации")
        String organizationName,

        @Schema(description = "Название подразделения")
        String departmentName

) implements HasEmployeeData {

}
