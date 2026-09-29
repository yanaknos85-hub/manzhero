package ru.sber.transport.tariff_fleet.service;


import ru.sber.transport.tariff_fleet.database.model.Employee;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with employees.
 */
public interface EmployeeService {

    /**
     * Get employee.
     *
     * @param id ID of employee.
     * @return employee.
     */
    Optional<Employee> get(UUID id);
    
    /**
     * Get employee by user ID.
     *
     * @param id ID of user.
     *
     * @return employee.
     */
    Employee getByUserId(UUID id);

    /**
     * Delete employee.
     *
     * @param entity employee to delete.
     */
    void delete(Employee entity);
    
    void saveOrUpdate(Employee entity);
    
}
