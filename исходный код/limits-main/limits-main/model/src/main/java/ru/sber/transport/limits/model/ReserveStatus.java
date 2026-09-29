package ru.sber.transport.limits.model;

/**
 * Статус резерва на операцию
 */
public enum ReserveStatus {

    /**
     * Зарезервировано
     */
    RESERVED,

    /**
     * Потрачено
     */
    SPENT,

    /**
     * Отменено
     */
    CANCELED
}
