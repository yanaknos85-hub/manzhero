package ru.sber.transport.tariff.external.providers.tariff.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ru.sber.transport.tariff.external.providers.model.YandexClass;
import ru.sber.transport.tariff.external.providers.tariff.serializing.SecondsToDurationDeserializer;
import ru.sber.transport.tariff.external.providers.tariff.serializing.YandexClassDeserializer;

import java.time.Duration;

/**
 * Параметры тарифа
 *
 * @param className - текстовое описание класса тарифа
 * @param price     - цена
 * @param waitTime  - время ожидания
 */
public record Option(
        @JsonProperty("class_name")
        @JsonDeserialize(using = YandexClassDeserializer.class)
        YandexClass className,
        double price,
        @JsonProperty("waiting_time")
        @JsonDeserialize(using = SecondsToDurationDeserializer.class)
        Duration waitTime
) {
}
