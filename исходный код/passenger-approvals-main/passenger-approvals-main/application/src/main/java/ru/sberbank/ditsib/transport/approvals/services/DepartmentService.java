package ru.sberbank.ditsib.transport.approvals.services;


import ru.sberbank.ditsib.transport.approvals.database.model.Department;

import java.util.Optional;
import java.util.Set;
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
     * Поиск подразделения владельца headId
     * @param headId
     * @return
     */
    Set<Department> findByDepartmentHeadId(UUID headId);

    /**
     * Сохраняем подразделение, которую мы получим по grpc из сервиса corporate
     *
     * @param message Сообщение в случае ошибки
     * @param id Идентификатор записи о подразделении
     */
    void saveGrpcEntity(String message, UUID id);
}
