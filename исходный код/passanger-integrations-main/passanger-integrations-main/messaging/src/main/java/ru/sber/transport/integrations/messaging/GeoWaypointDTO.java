package ru.sber.transport.integrations.messaging;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.LocalDateTime;

/**
 * Данные точки.
 *
 * @param geoPoint  Координаты
 * @param pointTime Время точки
 */
public record GeoWaypointDTO(
        @Schema(description = "Координаты")
        GeoPointDTO geoPoint,

        @Schema(description = "Время точки")
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        LocalDateTime pointTime
) {
}

