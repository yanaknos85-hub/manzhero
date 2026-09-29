package ru.sber.transport.fraud.monitoring.model;

import java.util.UUID;

/**
 * Бизнес-объект должности сотрудника
 */
public interface Position {

    /**
     * Получить идентификатор должности
     *
     * @return идентификатор должности
     */
    UUID getId();

    /**
     * Получить название должности
     *
     * @return название должности
     */
    String getName();


}
