package ru.sber.transport.tariff_fleet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.sber.transport.tariff_fleet.constant.DocumentType;

import java.util.UUID;

@Schema(name = "TariffResponse", description = "Абстрактный класс ответа на запрос тарифов")
@Getter
@Setter
public abstract class AbstractTariffResponse {
    @Schema(description = "ID тарифа",
            requiredMode = Schema.RequiredMode.REQUIRED)
    protected UUID id;
    @Schema(description = "Человекопонятный ID тарифа",
            requiredMode = Schema.RequiredMode.REQUIRED)
    protected String humanReadableId;
    @Schema(description = "Вид услуги",
            requiredMode = Schema.RequiredMode.REQUIRED)
    protected DocumentType documentType;
    @Schema(description = "Корпоративный клиент",
            requiredMode = Schema.RequiredMode.REQUIRED)
    protected String organizationName;
    @Schema(description = "Контрагент",
            requiredMode = Schema.RequiredMode.REQUIRED)
    protected String contractorName;
    @Schema(description = "Номер договора",
            requiredMode = Schema.RequiredMode.REQUIRED)
    protected String contractNumber;
}
