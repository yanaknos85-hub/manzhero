package ru.sber.transport.tariff_fleet.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.sberbank.ditsib.converters.LocalDateSerializer;

import java.time.LocalDate;
import java.util.UUID;

public record ContractWithServicePointsFileDto(
        @Schema(description = "Имя контрагента",
                type = "string",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "ООО ААА",
                maxLength = 255,
                minLength = 1)
        String contractorName,
        @Schema(description = "Корпоративный клиент",
                type = "string",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "Байкальский банк",
                maxLength = 255,
                minLength = 1)
        String organizationName,
        @Schema(description = "ID договора",
                type = "string",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "97ac91e3-82cc-4c90-9920-37517e3dae1d",
                maxLength = 36,
                minLength = 36)
        UUID id,
        @Schema(description = "Номер договора в учетной системе",
                type = "string",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "4387345-1",
                maxLength = 255,
                minLength = 1)
        String number,
        @Schema(description = "Сумма контракта (с НДС)",
                type = "long",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "1000000",
                minimum = "0",
                maximum = "99999999999")
        Long amountWithVat,
        @Schema(description = "Сумма контракта (без НДС)",
                type = "long",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "1000000",
                minimum = "0",
                maximum = "99999999999")
        Long amountWithoutVat,
        @Schema(description = "Начало периода действия договора",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "2025-01-01")
        @JsonSerialize(using = LocalDateSerializer.class)
        LocalDate start,
        @Schema(description = "Конец периода действия договора",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "2025-01-01")
        @JsonSerialize(using = LocalDateSerializer.class)
        LocalDate end,
        @Schema(description = "Статус договора",
                type = "boolean",
                requiredMode = Schema.RequiredMode.REQUIRED)
        boolean active,
        @Schema(description = "Логотип",
                type = "string",
                format = "binary",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String logo,
        @Schema(description = "Возможность редактировать точки обслуживания",
                type = "boolean",
                requiredMode = Schema.RequiredMode.REQUIRED)
        boolean isEditablePoints,
        @Schema(description = "Имя для сервисных точек",
                type = "string",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "ул. Красная д.5",
                maxLength = 255,
                minLength = 1)
        String name,
        @Schema(description = "Файл адресов точек обслуживания",
                type = "string",
                format = "binary",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String file
) { }
