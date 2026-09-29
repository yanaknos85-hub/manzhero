package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.approvals.database.model.Employee;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Repository of employees
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    /**
     * Find employee by ID.
     *
     * @param userId ID of user.
     * @return employee.
     */
    Optional<Employee> findByUserId(UUID userId);

    @Query("select e from Employee e left join fetch e.approveDepartments where e.userId = :userId")
    Optional<Employee> findByUserIdWithApproveDocuments(UUID userId);

    @Query(value = "select me.department_id from approvals.message_employee me where me.user_id = :userId", nativeQuery = true)
    Optional<UUID> findDepartmentIdByUserId(UUID userId);

    @Query("select e.id from Employee e where e.departmentId in (:departmentIds)")
    List<UUID> findAllByDepartmentIdIs(Set<UUID> departmentIds);
}
