package ru.sber.transport.tariff.external.model;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Данные тарифа
 */
public interface Tariff {

    /**
     * Расстояние в метрах
     *
     * @return расстояние в метрах
     */
    long distance();

    /**
     * Тип тарифа
     *
     * @return тип тарифа
     */
    Type type();

    /**
     * Стоимость поездки в рублях
     *
     * @return стоимость поездки в рублях
     */
    BigDecimal price();

    /**
     * Время поездки
     *
     * @return время поездки
     */
    Duration time();

    /**
     * Время ожидания поездки
     *
     * @return время ожидания поездки
     */
    Duration waitTime();

}
