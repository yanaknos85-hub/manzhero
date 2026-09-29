package ru.sber.transport.limits.business.model;

/**
 * Статус лимита
 */
public enum Status {

    /**
     * Планирование
     */
    PLANNING,

    /**
     * Распределение
     */
    SHARED,

    /**
     * Закрыт
     */
    CLOSED
}
