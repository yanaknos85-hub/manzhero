package ru.sberbank.ditsib.transport.limits.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit_;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for working with limits.
 */
@Repository
public interface DepLimitRepository extends LimitRepository<DepLimit>, JpaSpecificationExecutor<DepLimit> {
    
    /**
     * Find limit by department.
     *
     * @param departmentId id of department
     *
     * @return limit.
     */
    @EntityGraph(attributePaths = Limit_.PARENT)
    List<DepLimit> findByDepartmentId(UUID departmentId);

    /**
     * Find limit by department and year.
     *
     * @param departmentId id of department
     * @param year year
     *
     * @return limit.
     */
    List<DepLimit> findByDepartmentIdAndYear(UUID departmentId, Integer year);
    
    /**
     * Find limit by department and year.
     * @param departmentId id of department
     * @param year year
     * @param limitStatus limitStatus
     * @param type type of limit.
     * @return limit.
     */
    List<DepLimit> findByDepartmentIdAndYearAndLimitStatusAndLimitType(UUID departmentId
            , Integer year, LimitStatus limitStatus, LimitType type);
    
    /**
     * Find limit by department and year and service type.
     *
     * @param departmentId id of department
     * @param year year
     * @param limitServiceType тип услуги
     * @param limitStatus status of limit.
     *
     * @return limit.
     */
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = Limit_.CHILDREN)
    Optional<DepLimit> findByDepartmentIdAndYearAndLimitServiceTypeAndLimitStatusNot(UUID departmentId, Integer year,
                                                                                     String limitServiceType, LimitStatus limitStatus);

    /**
     * Find limit by department and year and service type.
     *
     * @param departmentId id of department
     * @param year year
     * @param limitServiceType тип услуги
     * @param limitStatus status of limit.
     *
     * @return limit.
     */
    @Query("select depLimit from DepLimit depLimit " +
           "where depLimit.department.id = :departmentId " +
           "and depLimit.year = :year " +
           "and depLimit.limitServiceType = :limitServiceType " +
           "and depLimit.limitStatus <> :limitStatus")
    Optional<DepLimit> findByDepartmentIdAndYearAndLimitServiceTypeAndLimitStatusNotWithNoChildren(UUID departmentId, Integer year,
                                                                                                   String limitServiceType, LimitStatus limitStatus);


    /**
     * Поиск всех распределенных лимитов для департамента по году
     *
     * @param id идентификатор подразделения
     * @param year год
     * @param limitStatus статус лимита
     * @return лимиты
     */
    List<DepLimit> findByDepartmentIdAndYearAndLimitStatus(UUID id, int year, LimitStatus limitStatus);

    @Query(value = """
            select
            	coalesce (count(1), 0) != 0
            from
            	limits."limit" l
            where
            	l.parent_department_id in (:ids)
            	and l.limit_status != 'CLOSED'
            	and l."year" >= (
            	select
            		extract(year
            	from
            		CURRENT_DATE))
            """, nativeQuery = true)
    boolean checkHaveNotClosedChildLimits(List<UUID> ids);
}

