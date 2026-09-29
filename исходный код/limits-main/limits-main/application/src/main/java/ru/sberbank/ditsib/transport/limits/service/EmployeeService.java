package ru.sberbank.ditsib.transport.limits.service;

import ru.sberbank.ditsib.transport.limits.model.basic.Employee;

import java.util.*;

/**
 * Service for working with employees.
 */
public interface EmployeeService {
    
    /**
     * Save employee.
     *
     * @param employee employee.
     */
    Employee save(Employee employee);
    
    /**
     * Delete employee.
     *
     * @param employee employee.
     */
    void delete(Employee employee);
    
    /**
     * Get employee of organization.
     *
     * @param employeeId ID of employee.
     *
     * @return employee.
     */
    Optional<Employee> get(UUID employeeId);
    
    /**
     * Get all employee in list.
     *
     * @param listEmployeeId list ID of employee.
     *
     * @return employee.
     */
    List<Employee> getAllById(List<UUID> listEmployeeId);
    
    /**
     * Get employee by user ID.
     *
     * @param id ID of user.
     *
     * @return employee.
     */
    Optional<Employee> getByUserId(UUID id);
    
    /**
     * Get employee by personnel number.
     *
     * @param personnelNumber personnel number.
     *
     * @return employee.
     */
    Employee getByPersonnelNumber(String personnelNumber);
    
    /**
     * Получить список сотрудников по подразделению и флагу активности
     *
     * @param isActive флаг активности
     * @param departmentId подразделение
     *
     * @return список сотрудников
     */
    List<Employee> findByDepartmentIdAndActive(UUID departmentId, Boolean isActive);
    
    /**
     * Получить количество сотрудников по подразделению и флагу активности
     *
     * @param isActive флаг активности
     * @param departmentId подразделение
     *
     * @return список сотрудников
     */
    int countByDepartmentIdAndActive(UUID departmentId, Boolean isActive);
    
    /**
     * Получить количество сотрудников по организации и флагу активности
     *
     * @param isActive флаг активности
     * @param organizationId организация
     *
     * @return список сотрудников
     */
    int countByOrganizationIdAndActive(Collection<UUID> organizationId, Boolean isActive);

    /**
     * Get map of department and employees count.
     *
     * @param departments list of ids of departments to get data.
     * @param active employees activity flag.
     * @return map of departments and employees count.
     */
    Map<UUID, Long> countByDepartmentsAndActive(Set<UUID> departments, boolean active);
}
