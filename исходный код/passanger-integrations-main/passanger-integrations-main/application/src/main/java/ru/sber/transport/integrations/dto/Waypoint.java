package ru.sber.transport.integrations.dto;

import java.util.List;

/**
 * Waypoint
 */
public record Waypoint(
        String name,
        Double latitude,
        Double longitude,
        Integer waitTime,
        List<Contact> passengers
) {
}