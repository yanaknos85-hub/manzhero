package ru.sber.transport.tariff.external.providers.tariff.model;

import ru.sber.transport.tariff.external.model.Tariff;
import ru.sber.transport.tariff.external.model.Type;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Данные тарифа
 *
 * @param type     - тип тарифа
 * @param price    - стоимость поездки
 * @param time     - время поездки
 * @param waitTime - время ожидания
 * @param distance - расстояние в метрах
 */
public record YandexTariff(Type type, BigDecimal price, Duration time, Duration waitTime,
                           long distance) implements Tariff {
}
