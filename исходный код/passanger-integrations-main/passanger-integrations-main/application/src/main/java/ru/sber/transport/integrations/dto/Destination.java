package ru.sber.transport.integrations.dto;

import jakarta.validation.Valid;

/**
 * Destination
 */
public record Destination(
        String name,
        Double latitude,
        Double longitude,
        @Valid
        Contact contact
) {
}

