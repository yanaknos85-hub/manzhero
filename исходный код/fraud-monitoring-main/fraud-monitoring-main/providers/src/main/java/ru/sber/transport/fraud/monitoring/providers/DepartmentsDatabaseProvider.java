package ru.sber.transport.fraud.monitoring.providers;


import ru.sber.transport.fraud.monitoring.model.Department;

import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер данных о подразделениях.
 */
public interface DepartmentsDatabaseProvider {

    /**
     * Сохраняет данные о подразделении
     *
     * @param source данные о подразделении
     * @return сохраненные данные о подразделении
     */
    Department createOrUpdate(Department source);

    /**
     * Получает данные о подразделении
     *
     * @param id идентификатор подразделения
     * @return данные о подразделении
     */
    Department get(UUID id);

    /**
     * Получает данные о подразделении без имени
     * @return данные о подразделении
     */
    Optional<UUID> getWithoutName();

}
