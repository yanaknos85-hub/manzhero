package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/// Сотрудник с сокращённым набором полей как заказчик для групп исполнителей
///
/// @param id Идентификатор сотрудника
 /// @param firstName Имя сотрудника
 /// @param lastName Фамилия сотрудника
 /// @param patronymic Отчество сотрудника
 /// @param personnelNumber Табельный номер сотрудника
@Schema(title = "Информация о сотруднике-заказчике для групп исполнителей", description = "Данные сотрудника")
public record CustomerDTO (

        @Schema(description = "Идентификатор")
        UUID id,

        @Schema(description = "Имя")
        String firstName,

        @Schema(description = "Фамилия")
        String lastName,

        @Schema(description = "Отчество")
        String patronymic,

        @Schema(description = "Табельный номер")
        String personnelNumber
) {}