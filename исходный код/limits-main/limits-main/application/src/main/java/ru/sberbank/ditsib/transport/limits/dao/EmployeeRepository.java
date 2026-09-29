package ru.sberbank.ditsib.transport.limits.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


/**
 * Employee repository
 */
@Repository
@Transactional(readOnly = true)
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    
    /**
     * Получить список сотрудников по флагу активности
     *
     * @param personnelNumber personnel number
     *
     * @return список сотрудников
     */
    List<Employee> findByPersonnelNumber(String personnelNumber);
    
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
     * Получить количество сотрудников по подразделению и флагу активности
     *
     * @param isActive флаг активности
     * @param organizationId огранизация
     *
     * @return список сотрудников
     */
    int countByOrganizationIdInAndActive(Collection<UUID> organizationId, Boolean isActive);
    
    /**
     * Получить список сотрудников по флагу активности
     *
     * @param isActive флаг активности
     *
     * @return список сотрудников
     */
    List<Employee> findByActive(boolean isActive);
    
    /**
     * Find employee by ID.
     *
     * @param userId ID of user.
     *
     * @return employee.
     */
    Optional<Employee> findByUserId(UUID userId);
}
