package ru.sber.transport.limits.model;

import java.util.UUID;

/**
 * Сотрудник
 */
public interface Employee {

    /**
     * Получить идентификатор сотрудника
     *
     * @return идентификатор сотрудника
     */
    UUID id();

    /**
     * Получить идентификатор отдела сотрудника
     *
     * @return идентификатор отдела сотрудника
     */
    UUID departmentId();
}
