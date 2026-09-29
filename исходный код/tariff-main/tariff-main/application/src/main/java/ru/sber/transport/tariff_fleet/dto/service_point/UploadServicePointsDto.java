package ru.sber.transport.tariff_fleet.dto.service_point;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "UploadServicePointsDto", description = "Провалидированные данные для загрузки сервисных точек")
public record UploadServicePointsDto(
        @Schema(description = "Точки обслуживания",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<ValidatedServicePointDto> servicePoints,
        @Schema(description = "Файл с описанием сервисных точек",
                requiredMode = Schema.RequiredMode.REQUIRED)
        byte[] file
) {
}
