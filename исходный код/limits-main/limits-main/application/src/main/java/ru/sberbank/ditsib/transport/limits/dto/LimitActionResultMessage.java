package ru.sberbank.ditsib.transport.limits.dto;

import java.util.UUID;

/**
 * Сообщение с результатом действия с лимитами.
 *
 * @param tripRequestId идентификатор заявки на поездку.
 * @param limitId идентификатор лимита.
 * @param message сообщение.
 * @param humanReadableId человекочитаемый идентификатор.
 * @param limitReservationStatus статус резервирования.
 */
public record LimitActionResultMessage(
        UUID tripRequestId,
        UUID limitId,
        String message,
        String humanReadableId,
        String limitReservationStatus
) {
}
