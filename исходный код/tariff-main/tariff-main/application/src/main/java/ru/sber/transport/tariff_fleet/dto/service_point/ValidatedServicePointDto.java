package ru.sber.transport.tariff_fleet.dto.service_point;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.tariff_fleet.service.excel.ExcelFieldInfo;

import java.math.BigDecimal;
import java.util.List;

@Schema(name = "ValidatedServicePointDto", description = "Данные после валидации точки обслуживания")
public record ValidatedServicePointDto(
        @Schema(description = "Номер по порядку",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "1")
        Integer number,
        @Schema(description = "Адрес точки обслуживания",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "ул. Зеленая, д.5")
        String address,
        @Schema(description = "Широта",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "55.756086")
        BigDecimal latitude,
        @Schema(description = "Долгота",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "55.756086")
        BigDecimal longitude,
        @Schema(description = "Ошибка",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "Не заполнены поля: Адрес")
        String error
) {
    
    public static List<ExcelFieldInfo> getExcelFieldsInfo() {
        return List.of(
                new ExcelFieldInfo("Номер по порядку", "number"),
                new ExcelFieldInfo("Адрес точки обслуживания", "address"),
                new ExcelFieldInfo("Широта", "latitude"),
                new ExcelFieldInfo("Долгота", "longitude"),
                new ExcelFieldInfo("Ошибка", "error"));
    }
}
