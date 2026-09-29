package ru.sber.transport.fraud.monitoring.business;


import ru.sber.transport.fraud.monitoring.model.Department;

import java.util.UUID;

/**
 * Сервис работы с подразделениями
 */
public interface DepartmentsService {

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
    Department getExistedOrCreate(UUID id);

}
