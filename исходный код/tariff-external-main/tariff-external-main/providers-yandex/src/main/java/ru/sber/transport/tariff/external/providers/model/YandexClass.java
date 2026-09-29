package ru.sber.transport.tariff.external.providers.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sber.transport.tariff.external.model.Type;

import java.util.Arrays;

/**
 * Типы тарифов для яндекс-такси
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum YandexClass {

    /**
     * Эконом
     */
    ECONOM(Type.ECONOMY),

    /**
     * Комфорт
     */
    BUSINESS(Type.COMFORT),

    /**
     * Комфорт+
     */
    COMFORTPLUS(Type.BUSINESS),

    /**
     * VIP
     */
    VIP(Type.VIP);

    /**
     * Внутренний тип тарифа
     */
    private final Type type;

    /**
     * Получить внешний тип тарифа
     *
     * @param type внутренний тип тарифа
     * @return внешний тип тарифа
     */
    public static YandexClass valueOf(Type type) {
        return Arrays.stream(values()).filter(it -> it.getType().equals(type)).findFirst().orElseThrow(() -> new IllegalArgumentException("Type %s is not found".formatted(type)));
    }
}
