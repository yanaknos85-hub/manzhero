package ru.sber.transport.tariff_fleet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.tariff_fleet.constant.DocumentType;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "SearchTariffDto", description = "Абстрактный запрос на поиск тарифов")
public class SearchTariffResponse {
    @Schema(description = "Типы договоров и тарифов")
    private DocumentType documentType;
    @Schema(description = "ID тарифа", example = "001b5a57-0a8f-4e7b-9c32-21c18c39fb8c")
    private UUID id;
    @Schema(description = "Человекопонятный ID тарифа", example = "TF-0001-00005589")
    private String humanReadableId;
    @Schema(description = "Корпоративный клиент", example = "Байкальский банк")
    private String organizationName;
    @Schema(description = "Контрагент", example = "Супер Сервис")
    private String contractorName;
    @Schema(description = "Наименование подразделения", example = "ЦА")
    private String departmentName;
    @Schema(description = "Номер договора", example = "4387345-1")
    private String contractNumber;
    @Schema(description = "Статус тарифа", example = "active")
    private boolean active;
}
