package ru.sberbank.ditsib.transport.limits.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit_;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Repository for working with limits.
 *
 * @param <T> type of period.
 */
@Repository
@Transactional(readOnly = true)
public interface LimitRepository<T extends Limit> extends JpaRepository<T, UUID>, JpaSpecificationExecutor<T> {
    
    
    /**
     * Find limit by parent limit.
     *
     * @param parent parent limit
     *
     * @return limit.
     */
    Set<Limit> findByParent(Limit parent);
    
    /**
     * Find limit by parent limit.
     *
     * @param parent parent limit
     * @param limitType type of limit
     *
     * @return limit.
     */
    Set<Limit> findByParentAndLimitType(Limit parent, LimitType limitType);
    
    /**
     * Find limit by limit sharing type.
     *
     * @param limitSharingType limitSharingType
     *
     * @return limit.
     */
    List<Limit> findByLimitSharingType(LimitSharingType limitSharingType);
    
    /**
     * Find limit by organizationId.
     *
     * @param organizationId Id organization
     *
     * @return limit.
     */
    List<Limit> findByOrganizationIdAndParentIdIsNull(UUID organizationId);
    
    /**
     * Find limit by year.
     *
     * @param organizationId ID of organization
     * @param year year
     *
     * @return limit.
     */
    List<Limit> findByOrganizationIdAndYear(UUID organizationId, Integer year);
    
    /**
     * Find limit by year.
     *
     * @param organizationId ID of organization
     * @param year year
     *
     * @return limit.
     */
    List<Limit> findByOrganizationIdAndYearAndParentIdIsNull(UUID organizationId, Integer year);
    
    /**
     * Find limit by year.
     *
     * @param organizationId ID of organization
     * @param limitServiceType service type of limit
     * @param year year
     *
     * @return limit.
     */
    List<Limit> findByOrganizationIdAndLimitServiceTypeAndYearAndParentIdIsNull(
            UUID organizationId, String limitServiceType,
            Integer year);
    
    /**
     * Find limit by year.
     *
     * @param organizationId ID of organization
     * @param limitServiceType service type of limit
     * @param year year
     * @param limitStatus status of limit
     *
     * @return limit.
     */
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = Limit_.CHILDREN)
    List<Limit> findByOrganizationIdAndLimitServiceTypeAndYearAndParentIdIsNullAndLimitStatusNot(
            UUID organizationId, String limitServiceType, Integer year, LimitStatus limitStatus);

    /**
     * Find limit by year.
     *
     * @param organizationId ID of organization
     * @param limitServiceType service type of limit
     * @param year year
     * @param limitStatus status of limit
     *
     * @return limit.
     */
    @Query("select limit " +
           "from Limit limit " +
           "inner join limit.organization organization " +
           "where organization.id = :organizationId " +
           "AND limit.limitServiceType = :limitServiceType " +
           "AND limit.year = :year " +
           "AND limit.limitStatus != :limitStatus")
    List<Limit> findByOrganizationIdAndLimitServiceTypeAndYearAndParentIdIsNullAndLimitStatusNotWithoutChildren(
            UUID organizationId, String limitServiceType, Integer year, LimitStatus limitStatus);

    /**
     * Find all limits by IDs of parents not in status
     *
     * @param ids list of parent IDs.
     * @param limitStatus status of limit to exclude from request.
     * @return list of limits.
     */
    List<Limit> findByParentIdInAndLimitStatusNot(Set<UUID> ids, LimitStatus limitStatus);

    @Query(value = """
            select
            	coalesce (count(1), 0) != 0
            from
            	limits."limit" l
            where
            	l.parent_id = :id
            	and l.limit_status != 'CLOSED'
            	and l."year" >= (
            	select
            		extract(year
            	from
            		CURRENT_DATE))
            """, nativeQuery = true)
    boolean checkHaveNotClosedChildLimits(UUID id);
}

