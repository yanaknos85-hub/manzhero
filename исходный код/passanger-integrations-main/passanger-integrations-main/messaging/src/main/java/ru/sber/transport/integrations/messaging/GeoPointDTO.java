package ru.sber.transport.integrations.messaging;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Объект обмена геоданными.
 *
 * @param latitude  Широта
 * @param longitude Долгота
 */
public record GeoPointDTO(
        @Schema(description = "Широта")
        double latitude,
        @Schema(description = "Долгота")
        double longitude
) {
}

