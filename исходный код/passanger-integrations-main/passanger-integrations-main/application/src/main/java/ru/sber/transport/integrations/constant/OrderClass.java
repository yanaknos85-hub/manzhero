package ru.sber.transport.integrations.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Класс поездки
 */

@Getter
@RequiredArgsConstructor
public enum OrderClass {
    ECONOMY("ECONOMY"),
    COMFORT("COMFORT"),
    COMFORT_PLUS("COMFORT_PLUS"),
    BUSINESS("BUSINESS"),
    GROUP_TRANSFER("GROUP_TRANSFER");
    
    private final String value;
}

