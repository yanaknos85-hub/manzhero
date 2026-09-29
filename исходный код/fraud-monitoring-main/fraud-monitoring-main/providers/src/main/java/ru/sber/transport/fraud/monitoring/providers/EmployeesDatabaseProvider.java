package ru.sber.transport.fraud.monitoring.providers;


import ru.sber.transport.fraud.monitoring.model.Employee;

import java.util.UUID;

/**
 * Провайдер данных о сотрудниках.
 */
public interface EmployeesDatabaseProvider {

    /**
     * Сохраняет сотрудника
     *
     * @param source данные сотрудника
     * @return сохраненный сотрудник
     */
    Employee createOrUpdate(Employee source);

    /**
     * Получает сотрудника по идентификатору
     *
     * @param id идентификатор сотрудника
     * @return сотрудник
     */
    Employee get(UUID id);
}
