package ru.sber.transport.corporate.business.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/// Допустимые типы контактов
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum ContactType {

    /// Номер телефона
    PHONE("\\+?[0-9][-\\s]?[-0-9]+"),

    /// Адрес электронной почты
    EMAIL("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"),

    /// Адрес сайта
    SITE("^(http(s)?:\\/\\/)?([a-zA-ZА-ЯЁа-яё]*[.])?([a-zA-ZА-ЯЁа-яё0-9]*[.])([a-zA-ZА-ЯЁа-яё]*)(\\/[a-zA-ZА-ЯЁа-яё0-9]*(\\?([a-zA-ZА-ЯЁа-яё0-9]*=[a-zA-ZА-ЯЁа-яё0-9]*(&)?)+)?)?$");

    /// Регулярное выражение для проверки соответствия значения типу.
    private final String regexp;

}
