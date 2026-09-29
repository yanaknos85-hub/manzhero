package ru.sber.transport.corporate.business.providers;

import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.business.model.EmployeeFilter;

import java.util.*;

/**
 * Провайдер данных о сотрудниках
 */
public interface EmployeeProvider extends Provider<Employee, EmployeeFilter> {

    /**
     * Получить список сотрудников.
     *
     * @param source данные сотрудников.
     * @return список сотрудников.
     */
    List<Employee> get(Collection<Map.Entry<UUID, String>> source);

    /**
     * Получить сотрудника по идентификатору организации и идентификатору руководителя.
     *
     * @param organizationId идентификатор организации.
     * @param headId идентификатор руководителя.
     * @return сотрудник.
     */
    Optional<Employee> get(UUID organizationId, String headId);

    /**
     * Получить сотрудника по идентификатору или табельному номеру.
     *
     * @param id идентификатор сотрудника.
     * @param personalNumber табельный номер сотруднка.
     * @return сотрудник.
     */
    Optional<Employee> getByIdOrPersonalNumber(UUID id, String personalNumber);

}
