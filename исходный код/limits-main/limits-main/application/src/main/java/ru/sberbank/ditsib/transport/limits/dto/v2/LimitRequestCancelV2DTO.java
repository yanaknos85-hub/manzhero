package ru.sberbank.ditsib.transport.limits.dto.v2;

/**
 * Object with data of client.
 *
 * @param description description of limit cancellation.
 */
public record LimitRequestCancelV2DTO(String description) {
}
