package ru.sberbank.ditsib.transport.limits.service;

import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.EmpLimit;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with limits.
 */
public interface EmpLimitService {
    
    /**
     * Add limit.
     *
     * @param empLimit request.
     */
    EmpLimit add(EmpLimit empLimit);
    
    /**
     * Save limit.
     *
     * @param empLimit request.
     */
    void save(EmpLimit empLimit);
    
    /**
     * Delete limit.
     *
     * @param empLimit limit.
     */
    void delete(EmpLimit empLimit);
    
    /**
     * Get limit.
     *
     * @param id ID of request.
     *
     * @return limit.
     */
    Optional<EmpLimit> get(UUID id);
    
    /**
     * Get all limits.
     *
     * @return limit.
     */
    List<EmpLimit> getAll();
    
    /**
     * Get limit by employee.
     *
     * @param employeeId employee.
     *
     * @return limit.
     */
    List<EmpLimit> getByEmployee(UUID employeeId);
    
    /**
     * Get limit by employee.
     *
     * @param employee employee.
     *
     * @return limit.
     */
    List<EmpLimit> getByEmployee(Employee employee);
    
    /**
     * Get limit by employee.
     *
     * @param employee employee.
     *
     * @return limit.
     * @deprecated Вывести из эксплуатации!
     */
    @Deprecated(forRemoval = true)
    EmpLimit getByEmployeeAndYear(Employee employee, Integer year);
    
    /**
     * Get limit by employee.
     *
     * @param employee employee.
     *
     * @return limit.
     */
    EmpLimit getByEmployeeAndYearAndLimitServiceType(Employee employee, Integer year, String limitServiceType);
    
    /**
     * Create employee limit.
     *
     * @param employee employee.
     * @param author author.
     * @param parentDepLimit parent department limit
     *
     * @return empLimit created employee limit
     */
    EmpLimit getEmpLimit(Employee employee, Employee author, DepLimit parentDepLimit);
    
    /**
     * Create employee limit and transfer.
     *
     * @param employee employee.
     * @param author author.
     * @param depLimit parent department limit.
     * @param sum sum.
     * @param transportType transport type
     *
     * @return empLimit created employee limit
     */
    EmpLimit createEmpLimitAndTransfer(Employee employee, Employee author, DepLimit depLimit, BigDecimal sum,
                                       TransportTypeEnum transportType);
    
    /**
     * Close employee limit.
     *
     * @param empLimit employee limit.
     * @param authorId author.
     */
    void closeEmpLimit(EmpLimit empLimit, UUID authorId);
    
}
