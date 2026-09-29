package ru.sber.transport.tariff.external.model;

/**
 * Координаты
 */
public interface Coordinates {

    /**
     * Получить широту
     * @return широта
     */
    double latitude();

    /**
     * Получить долготу
     * @return долгота
     */
    double longitude();

}
