package ru.sberbank.ditsib.corpclient.service;

import ru.sber.transport.authorization.annotations.Organization;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.dto.*;

import jakarta.validation.constraints.NotNull;
import java.util.*;

/**
 * Сервис по работе с подразделениями.
 */
public interface DepartmentService {
    
    /**
     * Сохранить данные подразделения.
     *
     * @param organizationId идентификатор организации.
     * @param source данные подразделения для обновления.
     *
     * @return созданное подразделение.
     */
    DepartmentDTO saveDepartment(@NotNull UUID organizationId, @NotNull NewDepartmentDTO source);
    
    /**
     * Достать верхнеуровеневый департамент для организации.
     *
     * @param organizationId айди организации
     *
     * @return список департаментов
     */
    List<Department> getUpperLevelDepartments(@NotNull UUID organizationId);

    /**
     * Достать департаменты для организации
     *
     * @param organizationId ID организации.
     *
     * @return список департаментов
     */
    List<Department> findAllByOrganizationId(UUID organizationId);
    
    /**
     * @param id identifier of department
     *
     * @return found department
     */
    Department getDepartment(@NotNull UUID id);
    
    /**
     * @param id identifier of department
     *
     * @return found department
     */
    DepartmentDTO getDepartmentDto(@NotNull UUID id);
    
    /**
     * @param id identifier of department
     * @param orgId of organization
     *
     * @return found department
     */
    DepartmentDTO validateDepartmentByIdAndOrgId(@NotNull UUID id, @NotNull UUID orgId);
    
    /**
     * @param id identifier of department to delete
     */
    void deleteDepartment(@NotNull UUID id);
    
    /**
     * @param source data for update
     *
     * @return updated entity
     */
    DepartmentDTO updateDepartment(@NotNull UUID organizationId, @NotNull UUID departmentId, @NotNull NewDepartmentDTO source);
    
    /**
     * Получить список активных подразделений организации
     *
     * @return collection of departments.
     */
    Iterable<DepartmentSelectDTO> getActiveDepartmentsDto(
            @NotNull UUID orgId, DepartmentParameters parameters, DepartmentProjection projection
                                                   );
    /**
     * Получить список всех подразделений по нескольким организациям с учётом фильтров
     *
     * @return collection of departments.
     */
    Iterable<DepartmentSelectDTO> getDepartmentsDto(
            Set<UUID> orgIds, Set<UUID> departments, DepartmentParameters parameters, DepartmentProjection projection);

    /**
     * Получить список активных подразделений организации
     *
     * @return collection of departments.
     */
    Iterable<DepartmentSelectDTO> getActiveDepartmentsDto(
            Set<UUID> departmentSet, @NotNull UUID orgId, DepartmentParameters parameters, DepartmentProjection projection
                                                   );
    
    /**
     * Получить список всех подразделений организации
     *
     * @return collection of departments.
     */
    Iterable<DepartmentDTO> getAllDepartmentsDto(
            @NotNull UUID orgId, DepartmentParameters parameters
                                                );
    
    /**
     * Get all departments of organization.
     *
     * @return collection of departments.
     */
    Iterable<Department> getDepartments(
            @NotNull UUID orgId, DepartmentParameters parameters
                                       );
    
    /**
     * @param id идентификатор департамента
     *
     * @return true если сушествует
     */
    boolean existsById(@NotNull UUID id);
    
    /**
     * @param departmentId идентификатор департамента
     *
     * @return коллекция департаментов
     */
    Iterable<DepartmentDTO> getChildrenDepartments(
            @NotNull UUID departmentId, DepartmentParameters parameters
                                                       );
    
    /**
     * Получение подразделения.
     *
     * @param id идентификатор организации.
     * @param code код подразделения.
     *
     * @return подразделение.
     */
    Optional<Department> getDepartment(UUID id, String code);

    /**
     * Получение подразделения.
     *
     * @param id идентификатор организации.
     * @param name название подразделения.
     *
     * @return подразделение.
     */
    Optional<Department> getDepartmentByName(UUID id, String name);
    
    /**
     * Сохранение подразделения.
     *
     * @param department подразделение.
     */
    void save(Department department);
    
    /**
     * Получение всех подразделений.
     *
     * @return подразделения.
     */
    Iterable<Department> getAll(DepartmentParameters parameters);
    
    /**
     * Получение всех подразделений.
     *
     * @return подразделения.
     */
    Iterable<Department> getAll();
    
    /**
     * Определение зацикливания родитель-ребенок.
     *
     * @param departmentId идентификатор изменяемого подразделения.
     * @param parentId идентификатор родительского подразделения..
     *
     * @return идентификатор зацикленного подразделения.
     */
    Optional<UUID> detectLooping(UUID departmentId, UUID parentId);

    /**
     * Получение верхнеруховневых подразделений.
     *
     * @param organizationId идентификатор организации.
     *
     * @return список подразделений.
     */
    Collection<Department> getUpperLevelActiveDepartments(UUID organizationId);

    /**
     * Получение верхнеруховневых подразделений.
     *
     * @param organizationId идентификатор организации.
     *
     * @return список подразделений.
     */
    List<DepartmentDTO> getUpperLevelActiveDepartmentsDto(UUID organizationId);

    /**
     * Получение подразделений по уровням.
     *
     * @param organizationId идентификатор организации.
     *
     * @return список подразделений.
     */
    Collection<DepartmentDTO> getLevelDepartments(UUID organizationId);

    /**
     * Проверка всех флагов филиала.
     *
     * @param organizationId идентификатор организации.
     *
     * @return состояние флагов.
     */
    CheckFilialFlagResutlDTO checkFilialFlagAll(UUID organizationId);

    /**
     * Проверка всех флага филиала для подразделения.
     *
     * @param department идентификатор подразделения.
     *
     * @return true если флаги филиала найдены выше по дереву или в поддереве указанного подразделения, false иначе.
     */
    boolean checkFilialFlag(Department department);

    /**
     * Получение списка организаций как дерево.
     *
     * @param organizationId идентификатор организации.
     *
     * @return списка организаций как дерево.
     */
    Iterable<DepartmentTreeNodeDTO> getAllDepartmentsAsTree(@Organization UUID organizationId);

    /**
     * Проверка на существование подразделения с таким именем.
     *
     * @param id идентификатор подразделения.
     * @param name имя подразделения.
     * @param exclusion исключение из выборки.
     * @return <code>true</code> если подразделение с таким именем существует, <code>false</code> иначе.
     */
    boolean existsName(UUID id, String name, UUID exclusion);

    /**
     * Проверка на существование подразделения с таким кодом.
     *
     * @param id идентификатор подразделения.
     * @param code код подразделения.
     * @param exclusion исключение из выборки
     * @return <code>true</code> если подразделение с таким кодом существует, <code>false</code> иначе.
     */
    boolean existsCode(UUID id, String code, UUID exclusion);
}
