package ru.sber.transport.fraud.monitoring.business;


import ru.sber.transport.fraud.monitoring.model.Employee;

import java.util.UUID;

/**
 * Сервис работы с должностями
 */
public interface EmployeesService {

    /**
     * Сохраняет сотрудника
     *
     * @param source сотрудник для сохранения
     * @return сохраненный сотрудник
     */
    Employee createOrUpdate(Employee source);

    /**
     * Получает сотрудника по идентификатору
     *
     * @param id идентификатор сотрудника
     * @return сотрудник
     */
    Employee getExistedOrCreate(UUID id);
}
