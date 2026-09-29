package ru.sberbank.ditsib.transport.limits.service;

import ru.sberbank.ditsib.transport.limits.model.basic.Department;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with departments.
 */
public interface DepartmentService {
    
    /**
     * Get department by ID.
     *
     * @param id ID of department.
     *
     * @return department.
     */
    Optional<Department> get(UUID id);
    
    /**
     * Get all departments.
     *
     * @return list of departments.
     */
    List<Department> getAll();
    
    /**
     * Get department by ID.
     *
     * @param organizationId ID of organization.
     * @param id ID of department.
     *
     * @return department.
     */
    Optional<Department> get(UUID organizationId, UUID id);
    
    /**
     * Delete department.
     *
     * @param department department.
     */
    void delete(Department department);
    
    /**
     * Save department.
     *
     * @param department department to save.
     */
    Department save(Department department);
    
    /**
     * Check department existence.
     *
     * @param organizationId ID of organization.
     * @param departmentId ID of department.
     *
     * @return <code>true</code> if exists.
     */
    boolean exists(UUID organizationId, UUID departmentId);
    
    /**
     * Get departments by code.
     *
     * @return departments
     */
    Department getByCode(String code);
    
    /**
     * Get departments by parent.
     *
     * @return departments
     */
    List<Department> getByParent(UUID parentId);
    
    /**
     * Get departments by parent.
     *
     * @return departments
     */
    List<Department> getUpperLevelDepartment(UUID organizationId);
    
}
