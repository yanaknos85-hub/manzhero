package ru.sberbank.ditsib.corpclient.service;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.transport.scim.messages.AccountRoleLinkMessage;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.dto.*;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * Employee crud and other operations
 */
public interface EmployeeService {
    
    /**
     *
     * @param department идентификатор подразделения.
     * @param source data for creation or update
     *
     * @return createdEntity
     */
    EmployeeDTO saveEmployee(UUID department, @NotNull NewEmployeeDTO source);
    
    /**
     * @param source data for creation or update
     *
     * @return createdEntity
     */
    Employee saveEmployee(@NotNull Employee source, Collection<String> roles);
    
    /**
     * @param userId userId
     */
    void validateUser(UUID userId);
    
    /**
     * @param organizationId identifier of organization
     * @param id identifier of user
     *
     * @return found employee
     */
    EmployeeDTO getEmployeeByOrganizationIdAndUserId(@NotNull UUID organizationId, @NotNull UUID id);
    
    /**
     * @param userId identifier of user
     *
     * @return found employee
     */
    Employee getEmployeeByUserId(@NotNull UUID userId);
    
    /**
     * @param id identifier of user
     *
     * @return found employee
     */
    Employee getEmployeeById(@NotNull UUID id);
    
    /**
     * Get employee by id ond orgId throws EntityNotFoundResponseException in case of no result
     *
     * @param organizationId identifier of organization
     * @param id identifier of user
     *
     * @return found employee
     */
    EmployeeDTO validateAndGetEmployeeByIdAndOrgId(@NotNull UUID id, @NotNull UUID organizationId);
    
    /**
     * Get employee by id ond orgId throws EntityNotFoundResponseException in case of no result
     *
     * @param departmentId identifier of organization
     * @param id identifier of user
     *
     * @return found employee
     */
    Employee validateAndGetEmployeeByIdAndDepartmentId(@NotNull UUID id, @NotNull UUID departmentId);

    /**
     * @param id identifier of employee to delete
     */
    void deleteEmployee(@NotNull UUID id);
    
    /**
     * @param departmentId id подразделения, сотрудников которого нужно удалить
     */
    List<Employee> deleteEmployeeByDepartmentId(UUID departmentId);
    
    /**
     * @param source data for update
     * @param newData
     */
    Employee updateEmployee(@NotNull Employee source, NewEmployeeDTO newData);

    /**
     * Частичное обновление данных сотрудника.
     *
     * @param target цель обновления.
     * @param data новые данные.
     */
    void updateEmployee(@NonNull Employee target, Map<EmployeePatchField, Serializable> data);

    /**
     * Подписание Пдн сотрудником.
     *
     * @param userId идентификатор сотрудника.
     */
    void signPdn(@NonNull UUID userId);
    
    /**
     * Get all employees of organization.
     *
     * @param organizationId id of organization
     *
     * @return collection of employees.
     */
    Iterable<HasEmployeeData> getEmployeesByOrgId(UUID organizationId, EmployeeParameters parameters, EmployeeProjection projection);

    /**
     * Get all employees of organization.
     *
     * @param organizationId id of organization
     *
     * @return collection of employees.
     */
    List<Employee> findAllByOrganizationId(UUID organizationId);
    
    /**
     * Get all employees of department.
     *
     * @param departmentId id of department
     *
     * @return collection of employees.
     */
    Iterable<HasEmployeeData> getEmployeesByDepartmentId(UUID departmentId, EmployeeParameters parameters, EmployeeProjection projection);

    /**
     * Get all employees of department.
     *
     * @param departmentIds ids of departments
     * @param excludedEmployeeIds ids of excluded employees
     *
     * @return page of employees.
     */
    Page<Employee> getEmployeesByDepartmentIds(Collection<UUID> departmentIds, EmployeeParameters parameters, Collection<UUID> excludedEmployeeIds);
    
    
    /**
     * получить список сотрудников, имеющих указанную должность и организацию
     * @param orgId id организаии
     * @param positionId id должности
     *
     * @return список сотрудников
     */
    Iterable<EmployeeDTO> getEmployeesByOrganizationIdAndPositionId(UUID orgId,UUID positionId, EmployeeParameters parameters);

    /**
     *  Получить список сотрудников, состоящих в указанных организациях и департаментах
     *
     * @param organizations id организаций
     * @param departments id департаментов
     * @param token токен пользователя для проверки прав
     *
     * @return список сотрудников
     */
    Iterable<CustomerDTO> findEmployeesByOrganisationsAndDepartments(List<UUID> organizations, List<UUID> departments, Jwt token);
    
    /**
     * Search employees by FIO substring
     *
     * @param organizationId organizatin id
     * @param searchString search string
     *
     * @return list of qualified dtos
     */
    Iterable<EmployeeDTO> findByFIOLike(String searchString, UUID organizationId, EmployeeParameters parameters);

    /**
     * Поиск сотрудников по подстроке FIO и идентификатору организационной структуры, полученному при аутентификации
     *
     * @param searchString search string
     *
     * @return list of qualified dtos
     */
    Iterable<EmployeeDTO> findByFIOLike(String searchString, EmployeeParameters parameters, JwtAuthenticationToken authentication);

    /**
     * Search employees by FIO substring
     *
     * @param searchString search string
     *
     * @return list of qualified dtos
     */
    Iterable<EmployeeDTO> findByFIOLike(String searchString, EmployeeParameters parameters);

    /**
     * Search employees by FIO substring
     *
     * @param departmentId department id
     * @param searchString search string
     *
     * @return list of qualified dtos
     */
    Iterable<EmployeeDTO> findByDepartmentAndByFIOLike(String searchString, UUID departmentId, EmployeeParameters parameters);

    /**
     * Exists by personnel number and organization id
     *
     * @param personnelNumber personnel number
     * @param organizationId organization id
     *
     * @return true if value exists
     */
    boolean existsByPersonnelNumberAndOrganizationId(String personnelNumber, UUID organizationId);
    
    /**
     * Gets all employees
     *
     * @return list of qualified dtos
     */
    Iterable<Employee> getAllEmployees(EmployeeParameters parameters);
    
    /**
     * Gets all employees
     *
     * @return list of qualified dtos
     */
    Iterable<Employee> getAllEmployees();
    
    /**
     * Увеличения счетчика согласований.
     *
     * @param actionId идентификатор объекта, породившего согласование.
     * @param actorEmployeeId идентификатор сотрудника, породившего согласование.
     */
    void addApproval(UUID actionId, @NonNull UUID actorEmployeeId);
    
    /**
     * Уменьшение счетчика согласований.
     *
     * @param actionId идентификатор объекта, породившего согласование.
     * @param actorEmployeeId идентификатор сотрудника, породившего согласование.
     */
    void deleteApproval(UUID actionId, UUID actorEmployeeId);
    
    /**
     * @param id идентификатор сотрудника
     *
     * @return true если существует
     */
    boolean existsById(@NotNull UUID id);
    
    /**
     * Получение сотрудника по табельному номеру.
     *
     * @param personalNumber табельный номер.
     * @return сотрудник.
     */
    Optional<Employee> getEmployeeByPersonalNumberAndOrganizationId(String personalNumber, UUID organizationId);
    
    List<Employee> getEmployeesWIthAttributes();

    OrganizationSelectDTO getOrganizationByUserId(UUID userId);

    /**
     * Получить Сотрудника из Authentication
     * @param authentication Authentication
     * @return Сотрудник
     */
    Employee getAuthenticatedEmployee(JwtAuthenticationToken authentication);

    void update(UUID id, AccountRoleLinkMessage message);

}
