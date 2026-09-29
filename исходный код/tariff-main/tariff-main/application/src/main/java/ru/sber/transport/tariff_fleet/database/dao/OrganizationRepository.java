package ru.sber.transport.tariff_fleet.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.OrganizationNameWithDepartmentInfo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository of organizations
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    
    /**
     * Find all active organizations sorted by official name asc
     *
     * @return List of organizations sorted by official name asc
     */
    List<Organization> findAllByActiveTrueOrderByOfficialName();
    
    /**
     * Find all active departments by organization ids
     *
     * @param organizationIds list of organization ids
     *
     * @return List of organizations and departments info
     */
    @Query(value =
                   """
                    SELECT
                       o.id                AS organizationId,
                       o.official_name     AS officialName,
                       d.id                AS departmentId,
                       d.department_name   AS departmentName,
                       d.parent_id         AS parentId
                    FROM tariff_fleet.organization o
                    LEFT JOIN tariff_fleet.department d ON o.id = d.organization_id
                    WHERE d.active IS TRUE AND o.id IN (:organizationIds);
                   """,
           nativeQuery = true)
    List<OrganizationNameWithDepartmentInfo> findByIdsWithActiveDepartments(@Param("organizationIds") List<UUID> organizationIds);
    
    /**
     * Получение уникального идентификатора (числового) организации по идентификатору подразделения
     *
     * @param userId Идентификатор записи с таблицы corporate.user
     *
     * @return Уникальный идентификатор (числовой)
     */
    @Query(value = """
                        select o.digit_id
                        from tariff_fleet.organization o
                                 join tariff_fleet.employee e on e.organization_id = o.id
                        where e.user_id = :userId
                   """,
           nativeQuery = true)
    Optional<Long> findDigitIdByUserId(UUID userId);
}
