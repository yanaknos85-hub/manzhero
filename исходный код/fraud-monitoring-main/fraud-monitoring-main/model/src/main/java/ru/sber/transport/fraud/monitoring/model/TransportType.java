package ru.sber.transport.fraud.monitoring.model;

import lombok.RequiredArgsConstructor;

/**
 * Тип транспорта
 */
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public enum TransportType {
    /**
     * Яндекс Go
     */
    YANDEX,
    /**
     * Общественный транспорт
     */
    PUBLIC,
    /**
     * Личный транспорт
     */
    PERSONAL,
    /**
     * Такси
     */
    TAXI,
    /**
     * Каршеринг
     */
    CARSHARING,
    /**
     * Неизвестный тип транспорта
     */
    UNKNOWN
}
