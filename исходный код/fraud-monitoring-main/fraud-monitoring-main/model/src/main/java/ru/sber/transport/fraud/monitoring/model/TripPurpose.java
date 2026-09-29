package ru.sber.transport.fraud.monitoring.model;

import java.util.UUID;

/**
 * Интерфейс целей поездки
 */
public interface TripPurpose {

    /**
     * Идентификатор цели
     *
     * @return идентификатор подразделения
     */
    UUID getId();

    /**
     * Название цели
     *
     * @return название подразделения
     */
    String getLabel();

}
