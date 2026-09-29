package ru.sberbank.ditsib.transport.limits.service;

import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitTransferHistoryType;
import ru.sberbank.ditsib.transport.limits.dto.GetLimitInfoDto;
import ru.sberbank.ditsib.transport.limits.model.LimitData;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Service for working with limits.
 */

public interface LimitService {

    /**
     * Get limit.
     *
     * @param id ID of request.
     * @return limit.
     */
    Optional<Limit> get(UUID id);

    /**
     * Get limit children.
     *
     * @return limit
     */
    Set<Limit> getLimitChildren(Limit limit, LimitType limitType);

    /**
     * Gets upper level limit.
     *
     * @return limit
     */
    List<Limit> getUpperLevelLimitList(UUID organizationId, String serviceType, Integer year);

    /**
     * Gets upper level limit.
     *
     * @param organizationId id of an organization to get data.
     * @param serviceType type of service for getting data.
     * @param year year of limit.
     * @return limits list.
     */
    List<Limit> getUpperLevelActiveLimitList(UUID organizationId, String serviceType, Integer year);

    /**
     * Gets upper level limit without children.
     *
     * @param organizationId id of an organization to get data.
     * @param serviceType type of service for getting data.
     * @param year year of limit.
     * @return limits list.
     */
    List<Limit> getUpperLevelActiveLimitListNoChildren(UUID organizationId, String serviceType, Integer year);

    /**
     * Gets upper level limit.
     *
     * @return limit
     */
    Optional<Limit> getUpperLevelActiveLimit(UUID organizationId, String serviceType, Integer year);

    Optional<Limit> getUpperLevelActiveLimitNoChildren(UUID organizationId, String serviceType, Integer year);

    /**
     * Gets upper level limit.
     *
     * @return limit
     */
    List<Limit> getUpperLevelActiveLimit(UUID organizationId, Integer year);

    /**
     * Gets upper level limit.
     *
     * @return limit
     */
    List<Limit> getUpperLevelActiveLimit(UUID organizationId);

    /**
     * Gets by year.
     *
     * @return limit list
     */
    List<Limit> getByOrganizationIdAndYear(UUID organizationId, Integer year);

    /**
     * Передача средств между лимитами.
     *
     * @param source                      данные исходного лимита.
     * @param target                      данные целевого лимита.
     * @param sum                         sum.
     * @param authorId                    author.
     * @param allowTakingFromClosedSource разрешено работать с закрытыми лимитами.
     */
    default void transferSum(LimitData source, LimitData target,
                             BigDecimal sum, UUID authorId,
                             boolean allowTakingFromClosedSource) {
        transferSum(source, target, sum, authorId, LimitTransferHistoryType.GENERAL, allowTakingFromClosedSource);
    }

    /**
     * Передача средств между лимитами.
     *
     * @param source                      данные исходного лимита.
     * @param target                      данные целевого лимита.
     * @param sum                         sum.
     * @param authorId                    author.
     * @param allowTakingFromClosedSource разрешено работать с закрытыми лимитами.
     * @param historyType                 тип записи.
     */
    void transferSum(LimitData source, LimitData target,
                     BigDecimal sum, UUID authorId, LimitTransferHistoryType historyType,
                     boolean allowTakingFromClosedSource);

    /**
     * Search limits by limit sharing type.
     *
     * @param limitSharingType limit sharing type
     * @return list of limits found
     */
    List<Limit> getByLimitSharingType(LimitSharingType limitSharingType);

    /**
     * Get limit by date
     *
     * @param employee employee.
     * @param date date.
     * @return limit
     */
    GetLimitInfoDto getLimiInfoByDate(Employee employee, LocalDate date, TransportTypeEnum transportType);

    /**
     * Close limit.
     *
     * @param limit limit to close.
     * @param authorId ID of an author of limit.
     * @return result of limit closing.
     */
    boolean closeLimit(Limit limit, UUID authorId);

    /**
     * Get children map. Key is ID of parent, value is a list of limits.
     *
     * @param parentIds list of parent IDs.
     * @return children map.
     */
    Map<UUID, List<Limit>> getChildren(Set<UUID> parentIds);

    /**
     * Get map of a limit and a department.
     *
     * @param limitIds set of IDs of limits to get departments.
     * @return a limit-department map
     */
    Map<UUID, Department> getDepartments(Set<UUID> limitIds);
}
