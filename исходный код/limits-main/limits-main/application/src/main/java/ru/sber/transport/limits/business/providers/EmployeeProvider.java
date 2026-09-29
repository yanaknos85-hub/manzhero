package ru.sber.transport.limits.business.providers;

import lombok.NonNull;
import ru.sber.transport.limits.business.model.Employee;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Провайдер данных сотрудников
 */
public interface EmployeeProvider {

    /**
     * Получить данные сотрудника по идентификатору
     *
     * @param userId идентификатор сотрудника
     * @return данные сотрудника
     */
    Optional<Employee> get(UUID userId);

    /**
     * Получить адреса электронной почты сотрудников
     *
     * @param responsibles идентификаторы сотрудников
     * @return адреса электронной почты сотрудников
     */
    Set<String> getEmails(List<UUID> responsibles);

    /**
     * Получить данные текущего сотрудника
     *
     * @return текущий сотрудник
     */
    Employee current();

    /**
     * Проверка СМД-полномочий сотрудника
     *
     * @return результат проверки
     */
    boolean hasAccess();

}
