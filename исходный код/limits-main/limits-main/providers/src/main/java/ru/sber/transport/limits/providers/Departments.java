package ru.sber.transport.limits.providers;

import ru.sber.transport.limits.model.Department;

import java.util.UUID;

/**
 * Провайдер данных подразделений
 */
public interface Departments {

    /**
     * Возвращает подразделение по идентификатору
     *
     * @param id идентификатор подразделения
     * @return подразделение
     */
    Department get(UUID id);

}
