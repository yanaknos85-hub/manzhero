package ru.sber.transport.limits.providers;

import ru.sber.transport.limits.model.Employee;

import java.util.UUID;

/**
 * Провайдер сотрудников
 */
public interface Employees {

    /**
     * Возвращает сотрудника по его идентификатору
     * @param id идентификатор сотрудника
     * @return сотрудник
     */
    Employee get(UUID id);

}
