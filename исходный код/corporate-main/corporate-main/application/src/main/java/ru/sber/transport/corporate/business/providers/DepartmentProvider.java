package ru.sber.transport.corporate.business.providers;

import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.business.model.DepartmentFilter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Провайдер данных о подразделениях.
 */
public interface DepartmentProvider extends Provider<Department, DepartmentFilter> {

    /**
     * Поиск подразделений, где сотрудник является руководителем.
     *
     * @param employeeId идентификатор сотрудника.
     * @return список подразделений.
     */
    List<UUID> findOfHead(UUID employeeId);

    /**
     * Получение организаций подразделений.
     *
     * @param departments подразделения.
     * @return мап подразделений и организаций.
     */
    Map<UUID, UUID> getOrganizations(List<UUID> departments);

    /**
     * Заполнение руководителей подразделений данными вышестоящего подразделения если в текущем руководителя нет.
     *
     * @param organizationId идентификатор организации для которой заполняются руководители.
     */
    void fillHeads(UUID organizationId);

    /**
     * Получение подразделений без руководителей.
     * @param organizationId идентификатор организации.
     * @return список идентификаторов подразделений.
     */
    List<UUID> getHeadlessDepartments(UUID organizationId);
}
