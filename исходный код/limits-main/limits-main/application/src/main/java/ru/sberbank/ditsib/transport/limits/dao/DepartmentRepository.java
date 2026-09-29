package ru.sberbank.ditsib.transport.limits.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Department repository
 */
@Repository
@Transactional(readOnly = true)
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    
    /**
     * Получить список подразделений по код подразделения
     *
     * @param code код подразделения
     *
     * @return список подразделений
     */
    List<Department> findByCode(String code);
    
    /**
     * Получить список подразделений по родителю
     *
     * @param parentId id of parent
     *
     * @return список подразделений
     */
    List<Department> findByParentId(UUID parentId);
    
    /**
     * Find department of organization by ID.
     *
     * @param id ID of department.
     * @param organizationId ID of organization.
     *
     * @return department.
     */
    Optional<Department> findByIdAndOrganizationId(UUID id, UUID organizationId);

    /**
     * Find department of organization by ID.
     *
     * @param organizationId ID of organization.
     *
     * @return department.
     */
    List<Department> findAllByOrganizationIdAndActiveIsTrue(UUID organizationId);
    
    /**
     * Check department existence.
     *
     * @param organizationId ID of organization.
     * @param departmentId ID of department.
     *
     * @return <code>true</code> if department already exists.
     */
    boolean existsByOrganizationIdAndId(UUID organizationId, UUID departmentId);
    
    /**
     * Find upper level department of organization.
     *
     * @param organizationId ID of organization.
     *
     * @return list of departments
     */
    List<Department> findByOrganizationIdAndParentIdIsNullAndActiveTrue(UUID organizationId);
}
