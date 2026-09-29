package ru.sber.transport.limits.web.providers;

import ru.sber.transport.limits.business.model.Department;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Провайдер подразделений
 */
public interface DepartmentsProvider {

    /**
     * Получить подразделений по идентификаторам лимитов
     *
     * @param ids идентификаторы лимитов
     * @return подразделения в связке с лимитами
     */
    Map<UUID, Department> getOfLimits(List<UUID> ids);
}
