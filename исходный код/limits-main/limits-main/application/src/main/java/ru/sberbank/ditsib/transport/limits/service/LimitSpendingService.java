package ru.sberbank.ditsib.transport.limits.service;

import ru.sberbank.ditsib.transport.limits.constants.LimitSpendingStatus;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPerPeriod;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSpending;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;

import java.util.*;

/**
 * Service for working with LimitSpending objects.
 */
public interface LimitSpendingService {
    
    /**
     * Add limit spending.
     *
     * @param limitSpending limit spending.
     */
    LimitSpending add(LimitSpending limitSpending);
    
    /**
     * Save limit spending.
     *
     * @param limitSpending limit spending.
     */
    void save(LimitSpending limitSpending);
    
    /**
     * Delete limit spending.
     *
     * @param limitSpending limit spending.
     */
    void delete(LimitSpending limitSpending);
    
    /**
     * Get limitSpending.
     *
     * @param id ID of limitSpending.
     *
     * @return limit spending.
     */
    Optional<LimitSpending> get(UUID id);
    
    /**
     * Get all limit spendings.
     *
     * @return list of limit spending.
     */
    List<LimitSpending> getAll();
    
    /**
     * Get limit spending by request id.
     *
     * @param requestId ID of request.
     *
     * @return limit spending.
     */
    LimitSpending getByRequestId(UUID requestId);
    
    /**
     * Get limit spending by limitSharingPerPeriod.
     *
     * @param limitSharingPerPeriod limitSharingPerPeriod.
     *
     * @return list of limit spendings.
     */
    List<LimitSpending> getByLimitSharingPerPeriod(LimitSharingPerPeriod limitSharingPerPeriod);
    
    /**
     * Get limit spending by limit id and year.
     *
     * @param limitId limitId.
     *
     * @return list of limit spendings.
     */
    List<LimitSpending> getByLimit(UUID limitId);

    /**
     * Get map of spending IDs and periods.
     *
     * @param spendingIds ID of spending to get data.
     * @return map of spending periods.
     */
    Map<UUID, Period> getSpendingPeriods(Collection<UUID> spendingIds);

    /**
     * Get map of spending IDs and sharings.
     *
     * @param spendingIds ID of spending to get data.
     * @return map of spending sharings.
     */
    Map<UUID, UUID> getSpendingSharings(Collection<UUID> spendingIds);

    /**
     * Get a map of limit spendings by IDs of limits and not in a status.
     *
     * @param limitIds IDs of limits to get spendings.
     * @param limitSpendingStatus the status to exclude from a query.
     * @return the map of and limit ID and spendings.
     */
    Map<UUID, List<LimitSpending>> getByLimitAndStatusNot(Set<UUID> limitIds, LimitSpendingStatus limitSpendingStatus);
}
