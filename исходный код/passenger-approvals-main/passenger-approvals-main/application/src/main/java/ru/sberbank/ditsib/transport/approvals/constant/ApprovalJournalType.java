package ru.sberbank.ditsib.transport.approvals.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Доступные статусы согласований.
 */
@Getter
@AllArgsConstructor
public enum ApprovalJournalType {

    /**
     * Изменение заявки на поездку
     */
    UPDATE_TRIP_REQUEST,

    /**
     * Заявка на поездку
     */
    TRIP_REQUEST,

    /**
     * Завершенная поездка
     */
    FINAL_TRIP,

    /**
     * Совместная поездка
     */
    SHARED_RIDE_JOIN
}
