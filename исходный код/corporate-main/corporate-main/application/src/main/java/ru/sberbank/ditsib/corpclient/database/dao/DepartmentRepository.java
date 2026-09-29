package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.ActiveStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Department repository
 */
@Transactional
@Repository
public interface DepartmentRepository extends JpaSpecificationExecutor<Department>, JpaRepository<Department, UUID> {
    
    /**
     * find department by org id
     *
     * @param orgId organizationId
     * @param status status of department to find.
     *
     * @return list of qualified departments
     */
    Page<Department> getByOrganizationIdAndActiveStatus(UUID orgId, ActiveStatus status, Pageable pageable);
    
    /**
     * find department by deparmentName and org id
     *
     * @param id department id
     * @param orgId organizationId
     *
     * @return department if exists
     */
    Optional<Department> getByIdAndOrganizationId(UUID id, UUID orgId);
    
    
    /**
     * Поиск подразделения, подичненного сотруднику с указанным id
     *
     * @param headId id главы подразделения
     *
     * @return подразделение
     */
    @Query("SELECT id FROM Department WHERE head.id = :headId and activeStatus = 'ACTIVE'")
    List<UUID> getActiveByDepartmentHeadId(UUID headId);
    
    
    /**
     * Найти подразделение по коду и статусу активности
     *
     * @param code код подразделения
     * @param status статус активности
     * @param organizationId ID организации
     *
     * @return department if exists
     */
    Optional<Department> findByCodeAndActiveStatusAndOrganizationId(String code, ActiveStatus status, UUID organizationId);
    
    
    /**
     * Найти подразделение по коду и статусу активности
     *
     * @param code code of department
     * @param exclude исключенный id
     * @param organizationId ID организации
     *
     * @return department if exists
     */
    Optional<Department> findByCodeAndIdNotAndActiveStatusAndOrganizationId(String code, UUID exclude, ActiveStatus status, UUID organizationId);
    
    /**
     * Получить дочерние подразделения по id родителя
     * @param departmentId id родителя
     * @return Список дочерних подразделений
     */
    Page<Department> findDistinctByParentId(UUID departmentId, Pageable page);
    
    /**
     * Получение подразделения.
     *
     * @param organizationId идентификатор организации.
     * @param code код подразделения.
     * @return подразделение.
     */
    Optional<Department> findByOrganizationIdAndCode(UUID organizationId, String code);
    
    /**
     * Получение подразделений с указанным табельным номером руководителя.
     *
     * @param personnelNumber табельный номер руководителя.
     * @return подразделения.
     */
    @Query("SELECT dep FROM Department dep " +
            "INNER JOIN dep.head depHead INNER JOIN dep.organization org " +
            "WHERE depHead.personnelNumber = :personnelNumber AND org.id = :organizationId")
    List<Department> findAllByDepartmentHeadPersonnelNumberAndOrganizationId(String personnelNumber, UUID organizationId);
    
    /**
     * Достать верхнеуровеневый департамент для организации по статусу активности
     *
     * @param organizationId ID of organization.
     *
     * @return list of departments
     */
    List<Department> findAllByOrganizationIdAndActiveStatusAndParentIsNull(UUID organizationId, ActiveStatus status);

    /**
     * Достать департаменты для организации
     *
     * @param organizationId ID of organization.
     *
     * @return list of departments
     */
    List<Department> findAllByOrganizationId(UUID organizationId);

    /**
     * Достать верхнеуровеневый департамент для организации.
     *
     * @param organizationId ID of organization.
     *
     * @return list of departments
     */
    List<Department> findByOrganizationIdAndParentIsNull(UUID organizationId);

    List<Department> findByOrganizationIdAndParentId(UUID organizationId, UUID parent);

    /**
     * Проверка существования подразделения с именем в организации.
     *
     * @param organizationId идентификатор организации для поиска.
     * @param name название подразделения для поиска.
     * @return true, если подразделение с таким именем существует.
     */
    boolean existsByOrganizationIdAndName(UUID organizationId, String name);

    /**
     * Проверка существования подразделения с именем в организации.
     *
     * @param organizationId идентификатор организации для поиска.
     * @param name название подразделения для поиска.
     * @param exclusion список исключений.
     * @return true, если подразделение с таким именем существует.
     */
    boolean existsByOrganizationIdAndNameAndIdNotIn(UUID organizationId, String name, Set<UUID> exclusion);

    /**
     * Проверка существования подразделения с кодом в организации.
     *
     * @param organizationId идентификатор организации для поиска.
     * @param code код подразделения для поиска.
     * @return true, если подразделение с таким именем существует.
     */
    boolean existsByOrganizationIdAndCode(UUID organizationId, String code);

    /**
     * Проверка существования подразделения с кодом в организации.
     *
     * @param organizationId идентификатор организации для поиска.
     * @param code код подразделения для поиска.
     * @param exclusion список исключений.
     * @return true, если подразделение с таким именем существует.
     */
    boolean existsByOrganizationIdAndCodeAndIdNotIn(UUID organizationId, String code, Set<UUID> exclusion);

    /**
     * Найти подразделение по названию.
     *
     * @param organizationId идентификатор организации для поиска.
     * @param name название подразделения для поиска.
     * @return подразделение.
     */
    Optional<Department> findByOrganizationIdAndName(UUID organizationId, String name);
}
