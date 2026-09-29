package ru.sber.transport.limits.model;

import java.util.UUID;

/**
 * Модель подразделений
 */
public interface Department {

    /**
     * Идентификатор подразделения
     *
     * @return идентификатор подразделения
     */
    UUID id();

    /**
     * Идентификатор родительского подразделения
     *
     * @return идентификатор родительского подразделения, если не задан, то null
     */
    UUID parentId();

}
