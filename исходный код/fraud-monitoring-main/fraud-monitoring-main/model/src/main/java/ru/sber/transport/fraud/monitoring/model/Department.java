package ru.sber.transport.fraud.monitoring.model;

import java.util.UUID;

/**
 * Интерфейс подразделений
 */
public interface Department {

    /**
     * Идентификатор подразделения
     *
     * @return идентификатор подразделения
     */
    UUID getId();

    /**
     * Идентификатор руководителя
     *
     * @return идентификатор руководителя подразделения
     */
    UUID getHeadId();

    /**
     * Идентификатор родительского подразделения
     *
     * @return идентификатор родительского подразделения
     */
    UUID getParentId();

    /**
     * Название подразделения
     *
     * @return название подразделения
     */
    String getName();

    /**
     * Код подразделения
     *
     * @return код подразделения
     */
    String getCode();

}
