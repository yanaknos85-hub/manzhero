package ru.sber.transport.tariff.external.providers.tariff.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ru.sber.transport.tariff.external.providers.tariff.serializing.SecondsToDurationDeserializer;

import java.time.Duration;
import java.util.List;

/**
 * Описание яндекс-тарифа
 *
 * @param distance - дистанция
 * @param options  - параметры тарифа
 * @param time     - время в пути
 */
record YandexTariff(
        double distance,
        List<Option> options,
        @JsonDeserialize(using = SecondsToDurationDeserializer.class)
        Duration time
) {
}
