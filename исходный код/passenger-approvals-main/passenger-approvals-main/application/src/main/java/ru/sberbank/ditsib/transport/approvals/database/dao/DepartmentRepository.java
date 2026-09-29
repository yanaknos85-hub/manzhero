package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.approvals.database.model.Department;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Repository of department
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    Set<Department> findByDepartmentHeadId(UUID headId);

    /**
     * Получить подразделение по id с загруженными согласующими
     *
     * @param departamentId
     * @return
     */
    @Query("select d from Department d " +
            "left join fetch d.approvers where d.id = :departamentId")
    Optional<Department> findDepartmentWithApprovers(UUID departamentId);

}
