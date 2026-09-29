package ru.sberbank.ditsib.transport.limits.service;

import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharing;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPerPeriod;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Service for working with limit sharings per period.
 *
 * @param <T> type of period.
 */
public interface LimitSharingPerPeriodService<T extends Period> {
    
    /**
     * Add limit sharing per period.
     *
     * @param limitSharing limit sharing.
     * @param author author.
     * @param sum sum.
     * @param period period number.
     *
     * @return limit sharing per period
     */
    LimitSharingPerPeriod add(
            LimitSharing limitSharing, Employee author, BigDecimal sum,
            T period
                             );
    
    /**
     * Save limit sharing per period.
     *
     * @param limitSharingPerPeriod limit sharing per period.
     */
    void save(LimitSharingPerPeriod limitSharingPerPeriod);
    
    /**
     * Delete limit sharing per period.
     *
     * @param limitSharingPerPeriod limit sharing per period.
     */
    void delete(LimitSharingPerPeriod limitSharingPerPeriod);
    
    /**
     * Get limit sharing per period.
     *
     * @param id ID of limit sharing per period.
     *
     * @return limit sharing per period.
     */
    Optional<LimitSharingPerPeriod> get(UUID id);
    
    /**
     * Get all limit sharings per period.
     *
     * @return list of limit sharing per period.
     */
    List<LimitSharingPerPeriod> getAll();
    
    /**
     * Get list of limit sharings per period by limit sharing.
     *
     * @param limitSharing limit sharing
     *
     * @return map of limit sharings per period.
     */
    Map<T, LimitSharingPerPeriod> getByLimitSharing(LimitSharing limitSharing);
    
    /**
     * Gets limit sharing per period by limit sharing and period.
     *
     * @param limitSharing limit sharing
     * @param period period number
     *
     * @return limit sharings per period.
     */
    LimitSharingPerPeriod getByLimitSharingAndPeriod(LimitSharing limitSharing,
                                                     T period);
    
    /**
     * Gets period number for date.
     *
     * @param limit limit
     * @param date date
     *
     * @return limit sharing per period.
     */
    T getPeriodNumber(Limit limit, LocalDate date);
    
    /**
     * Gets period number for current date.
     *
     * @param limit limit
     *
     * @return limit sharing per period.
     */
    default T getCurrentPeriodNumber(Limit limit) {
        return getPeriodNumber(limit, LocalDate.now(ZoneOffset.UTC));
    }
    
    /**
     * Gets limit sharing per period by limit sharing and date.
     *
     * @param limitSharing limit sharing
     * @param date date
     *
     * @return limit sharing per period.
     */
    LimitSharingPerPeriod getForDate(LimitSharing limitSharing, LocalDate date);

    /**
     * Gets limit sharings per period by limit sharings and period and group by limit sharing.
     *
     * @param sharings limit sharings
     * @param date date
     *
     * @return Map of limit sharing per period by sharing.
     */
    Map<LimitSharing, LimitSharingPerPeriod> getPeriodsForDateBySharings(Collection<LimitSharing> sharings, LocalDate date);

    /**
     * Gets limit sharing per period by limit sharing and period.
     *
     * @param limitSharing limit sharing
     * @param period period
     *
     * @return limit sharing per period.
     */
    LimitSharingPerPeriod getForPeriod(LimitSharing limitSharing, T period);

    /**
     * Gets limit sharings per period by limit sharings and period and group by limit sharing.
     *
     * @param sharings limit sharings
     * @param period period
     *
     * @return Map of limit sharing per period by sharing.
     */
    Map<LimitSharing, LimitSharingPerPeriod> getPeriodsBySharings(Collection<LimitSharing> sharings, T period);


    /**
     * Get types of limit sharing.
     *
     * @return types of limit sharing.
     */
    List<LimitSharingType> types();
}
