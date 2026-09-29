package ru.sberbank.ditsib.transport.approvals.services;


import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.approvals.database.model.Employee;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service for working with employees.
 */
public interface EmployeeService {
    
    /**
     * Save employee.
     *
     * @param employee employee.
     * @return saved employee
     */
    Employee save(Employee employee);
    
    /**
     * Delete employee.
     *
     * @param employee employee.
     */
    void delete(Employee employee);
    
    /**
     * Get employee.
     *
     * @param id ID of employee.
     *
     * @return employee.
     */
    Optional<Employee> get(UUID id);

    /**
     * Return a employee by user id
     * @param userId
     * @return
     */
    Employee getByUserId(UUID userId) throws EntityNotFoundException;

    /**
     * Получить сотрудника со списоком департаментов, которые пользователь может апрувить
     * @param userId
     * @return
     */
    Employee getEmployeeByIdWithApproveDepartments(UUID userId) throws EntityNotFoundException;
    
    /**
     * Получить ID подразделения, где числиться сотрудник, по ID его пользователя
     * @param userId ID пользователя для сотрудника
     * @return ID подразделения, где числиться сотрудник
     * @throws EntityNotFoundException
     */
    UUID getDepartmentIdByUserId(UUID userId) throws EntityNotFoundException;

    List<UUID> getAllByDepartmentIds(Set<UUID> departmentIds);
}
