package ru.sber.transport.limits.business.providers;

import ru.sber.transport.limits.business.model.Department;

import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер подразделений
 */
public interface DepartmentsProvider {

    /**
     * Получить подразделение
     *
     * @param departmentId идентификатор подразделения
     * @return подразделение
     */
    Optional<Department> get(UUID departmentId);

}
