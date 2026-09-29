package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.corpclient.database.model.ActiveStatus;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.Employee_;
import ru.sberbank.ditsib.corpclient.database.model.OrgStructureType;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


/**
 * Репозиторий сотрудников.
 */
@Repository
public interface EmployeeRepository extends JpaSpecificationExecutor<Employee>, JpaRepository<Employee, UUID> {

    /**
     * Search for Employee by departmentId and userId
     *
     * @param departmentId id of organization
     * @param userId       search id
     * @return optional of search result
     */
    Optional<Employee> findByIdAndDepartmentId(UUID userId, UUID departmentId);

    /**
     * Search for Employee by organizationId and employee id
     *
     * @param organizationId id of organization
     * @param userId         search id
     * @return optional of search result
     */
    Optional<Employee> findByIdAndOrganizationId(UUID userId, UUID organizationId);

    /**
     * Search for Employee by department
     *
     * @param departmentId id of department
     * @return optional of search result
     */
    Page<Employee> findAllByDepartmentIdAndActiveStatus(UUID departmentId, ActiveStatus status, Pageable pageRequest);

    /**
     * Search for Employee by user id and org id
     *
     * @param organizationId organizationId
     * @param userId         search id
     * @return optional of search result
     */
    Optional<Employee> findByDepartmentOrganizationIdAndUserId(UUID organizationId, UUID userId);


    /**
     * Search for Employee by userId
     *
     * @param userId search id
     * @return optional of search result
     */
    Optional<Employee> findByUserId(UUID userId);

    /**
     * Search employees by FIO substring
     *
     * @param searchString search string
     * @return list of qualified entities
     */
    @EntityGraph(attributePaths = Employee_.DEPARTMENT, type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT employee from Employee employee where LOWER(" +
            "concat(employee.lastName,' ',employee.firstName,' '," +
            "CASE WHEN employee.patronymic is not null THEN employee.patronymic" +
            "                  ELSE ''" +
            "                  END))" +
            "like lower(concat('%', :searchString,'%')) and employee.position.organization.id =" +
            " :organizationId")
    Page<Employee> findByFIOLike(String searchString, UUID organizationId, Pageable pageable);

    /**
     * Search employees by FIO substring
     *
     * @param searchString search string
     * @return list of qualified entities
     */
    @EntityGraph(attributePaths = Employee_.DEPARTMENT, type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT employee from Employee employee where LOWER(" +
            "concat(employee.lastName,' ',employee.firstName,' '," +
            "CASE WHEN employee.patronymic is not null THEN employee.patronymic" +
            "                  ELSE ''" +
            "                  END))" +
            "  like lower(concat('%', :searchString,'%'))")
    Page<Employee> findByFIOLike(String searchString, Pageable pageable);

    /**
     * Search employees by FIO substring
     *
     * @param searchString search string
     * @return list of qualified entities
     */
    @EntityGraph(attributePaths = Employee_.DEPARTMENT, type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT employee from Employee employee where LOWER(" +
            "concat(employee.lastName,' ',employee.firstName,' '," +
            "CASE WHEN employee.patronymic is not null THEN employee.patronymic" +
            "                  ELSE ''" +
            "                  END))" +
            "  like lower(concat('%', :searchString,'%')) and employee.orgStructureType =" +
            " :orgStructureType")
    Page<Employee> findByFIOLike(String searchString,
                                 OrgStructureType orgStructureType,
                                 Pageable pageable);

    /**
     * Search employees by FIO substring
     *
     * @param searchString search string
     * @return list of qualified entities
     */
    @EntityGraph(attributePaths = Employee_.DEPARTMENT, type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT employee from Employee employee where LOWER(" +
            "concat(employee.lastName,' ',employee.firstName,' '," +
            "CASE WHEN employee.patronymic is not null THEN employee.patronymic" +
            "                  ELSE ''" +
            "                  END))" +
            "  like lower(concat('%', :searchString,'%')) and employee.orgStructureType =" +
            " :orgStructureType and employee.position.organization.id =" +
            " :organizationId")
    Page<Employee> findByFIOLike(String searchString,
                                 OrgStructureType orgStructureType,
                                 UUID organizationId,
                                 Pageable pageable);

    /**
     * Search employees by FIO substring
     *
     * @param departmentId departmentid
     * @param searchString search string
     * @return list of qualified entities
     */
    @Query("SELECT employee from Employee employee where LOWER(" +
            "concat(employee.lastName,' ',employee.firstName,' '," +
            "CASE WHEN employee.patronymic is not null THEN employee.patronymic" +
            "                  ELSE ''" +
            "                  END))" +
            " like lower(concat('%', :searchString,'%')) and employee.department.id =" +
            " :departmentId")
    Page<Employee> findByDepartmentAndByFIOLike(String searchString, UUID departmentId, Pageable pageable);

    /**
     * Find Employee by Personnel number
     *
     * @param personnelNumber табельный номер.
     * @return Personnel number
     */
    Optional<Employee> findByPersonnelNumber(String personnelNumber);

    /**
     * Find Employee by Personnel number
     *
     * @param personnelNumber табельный номер.
     * @return Personnel number
     */
    Optional<Employee> findByPersonnelNumberAndOrganizationId(String personnelNumber, UUID organizationId);

    /**
     * Find Employee by Personnel number
     *
     * @param personnelNumber табельный номер.
     * @return Personnel number
     */
    Optional<Employee> findByPersonnelNumberAndOrganizationIdAndIdNot(String personnelNumber, UUID organizationId, UUID excludes);

    /**
     * Получить всех сотрудников, связанных с согласованием.
     *
     * @param actionId идентификатор согласования.
     * @return сотрудники.
     */
    Page<Employee> findAllByActionsContains(UUID actionId, Pageable pageRequest);

    /**
     * Получить список активных/неактивных сотрудников с указанной должностью
     *
     * @param positionId id должносьт
     * @param status     статус
     * @return список сотрудников
     */
    Page<Employee> findAllByPositionIdAndActiveStatus(UUID positionId, ActiveStatus status, Pageable pageRequest);

    /**
     * Получить список сотрудников с указанной должностью
     *
     * @param orgId      id организаии
     * @param positionId id должности
     * @return список сотрудников
     */
    Page<Employee> findAllByPositionOrganizationIdAndPositionId(UUID orgId, UUID positionId, Pageable pageable);

    /**
     * Получить всех сотрудников, у которых выставлен табельный номер руководителя.
     *
     * @param personnelNumber табельный номер руководителя.
     * @return сотрудники.
     */
    @Query("SELECT emp FROM Employee emp INNER JOIN emp.organization org INNER JOIN emp.supervisor sup " +
            "WHERE sup.personnelNumber = :personnelNumber AND org.id = :organizationId")
    Page<Employee> findAllBySupervisorPersonnelNumberAndDepartmentOrganizationId(String personnelNumber, UUID organizationId, Pageable pageRequest);

    @Query("SELECT employee FROM Employee employee LEFT JOIN employee.attributes attr WHERE attr.id IS NOT NULL")
    List<Employee> findAllWithAttributes();

    boolean existsByUserId(UUID userId);

    @Transactional
    @Modifying
    @Query("update Employee employee set employee.activeStatus = 'INACTIVE' where employee.organization.id = :id")
    void deactivate(@Param("id") UUID id);

    List<Employee> findAllByOrganizationId(UUID organizationId);

    List<Employee> findAllByOrganizationIdInOrDepartmentIdIn(List<UUID> organizations, List<UUID> departments);
}
