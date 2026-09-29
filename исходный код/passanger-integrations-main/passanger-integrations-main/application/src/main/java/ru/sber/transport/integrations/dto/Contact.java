package ru.sber.transport.integrations.dto;

import ru.sberbank.ditsib.transport.constants.TaxiStopType;

/**
 * Contact
 * @param phone
 * @param mobilePhone
 * @param name
 * @param firstName
 * @param patronymic
 * @param type
 */
public record Contact(
        @Deprecated(since = "Оставил для обратной совместимости, актуальное mobilePhone")
        String phone,
        String mobilePhone,
        @Deprecated(since = "Оставил для обратной совместимости, актуальное firstName и patronymic")
        String name,
        String firstName,
        String patronymic,
        TaxiStopType type
) {
    
}

