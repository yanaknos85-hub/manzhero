package ru.sber.transport.limits.business.model;

/**
 * Типы распределения лимитов
 */
public enum SharingType {

    /**
     * Ежемесячно
     */
    MONTHLY,

    /**
     * Квартально
     */
    QUARTER,

    /**
     * Проценты
     */
    PERCENTS
}
