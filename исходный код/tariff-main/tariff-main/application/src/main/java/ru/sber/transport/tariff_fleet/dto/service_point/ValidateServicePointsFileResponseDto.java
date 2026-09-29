package ru.sber.transport.tariff_fleet.dto.service_point;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(title = "Отредактированные точки обслуживания", description = "Отредактированные данные для обновления сервисных точек")
public record ValidateServicePointsFileResponseDto(
        @Schema(description = "Точки обслуживания",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<ValidatedServicePointDto> servicePoints,
        @Schema(description = "Файл с описанием сервисных точек",
                requiredMode = Schema.RequiredMode.REQUIRED)
        byte[] file
) {
}
