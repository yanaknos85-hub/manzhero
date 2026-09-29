package ru.sber.transport.fraud.monitoring.providers;


import ru.sber.transport.fraud.monitoring.model.Department;

import java.util.UUID;

/**
 * Провайдер данных о подразделениях (RGRPC).
 */
public interface DepartmentsGrpcProvider {

    /**
     * Получает данные о подразделении
     *
     * @param id идентификатор подразделения
     * @return данные о подразделении
     */
    Department get(UUID id);

}
