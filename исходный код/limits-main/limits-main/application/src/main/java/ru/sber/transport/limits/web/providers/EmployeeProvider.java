package ru.sber.transport.limits.web.providers;

import ru.sber.transport.limits.business.model.Employee;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Провайдер сотрудников
 */
public interface EmployeeProvider {

    /**
     * Получить сотрудников по идентификаторам лимитов
     *
     * @param ids идентификаторы лимитов
     * @return сотрудники в связке с лимитами
     */
    Map<UUID, Employee> getOwnerOfLimits(List<UUID> ids);

    /**
     * Получить сотрудников по идентификаторам лимитов
     *
     * @param ids идентификаторы лимитов
     * @return сотрудники в связке с лимитами
     */
    Map<UUID, Employee> getEmployeeOfLimits(List<UUID> ids);
}
