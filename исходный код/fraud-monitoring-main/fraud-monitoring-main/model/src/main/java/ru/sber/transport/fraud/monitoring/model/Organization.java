package ru.sber.transport.fraud.monitoring.model;

import java.util.UUID;

/**
 * Модель организации
 */
public interface Organization {

    /**
     * Идентификатор организации
     *
     * @return идентификатор организации
     */
    UUID getId();

    /**
     * Порядковый номер организации
     *
     * @return порядковый номер организации
     */
    long getDigitId();

}
