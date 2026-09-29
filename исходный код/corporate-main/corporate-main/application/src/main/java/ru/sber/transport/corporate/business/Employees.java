package ru.sber.transport.corporate.business;

import lombok.NonNull;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.business.model.EmployeeFilter;
import ru.sber.transport.dto.Page;
import ru.sber.transport.web.model.Projection;
import ru.sberbank.ditsib.request.Direction;

import java.util.UUID;

/**
 * Набор операций для работы с сотрудниками.
 */
public interface Employees {

    /**
     * Получить основную информацию о сотруднике.
     *
     * @param employeeId идентификатор сотрудника.
     * @return сотрудник.
     */
    Employee get(UUID employeeId);

    /**
     * Получить страницу сотрудников
     *
     * @param filter фильтр.
     * @param projection проекция.
     * @param page номер страницы.
     * @param size количество элементов на странице.
     * @param sort сортировка.
     * @param direction направление сортировки.
     * @return страница сотрудников.
     */
    Page<Employee> get(@NonNull EmployeeFilter filter, @NonNull Projection projection, @NonNull Integer page, @NonNull Integer size, @NonNull String sort, @NonNull Direction direction);
}
