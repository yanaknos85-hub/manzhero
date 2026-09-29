package ru.sber.transport.corporate.utils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Список констант приложения.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum Constants {

    /**
     * Заполнение для объектов, используемый в системе, но не загруженных.
     */
    WAITING_FOR_DATA("Ожидание данных");

    private final String value;

}
