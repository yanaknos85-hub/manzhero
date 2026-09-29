package ru.sberbank.ditsib.transport.limits.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharing;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPerPeriod;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with LimitSharing objects.
 */
public interface LimitSharingService {
    
    /**
     * Add limit sharing.
     *
     * @param limitSharing LimitSharing.
     *
     * @return limit sharing
     */
    LimitSharing add(LimitSharing limitSharing);
    
    /**
     * Create limit sharing.
     *
     * @param limit limit.
     * @param transportType transport type.
     * @param author author.
     * @param sum sum.
     *
     * @return limit sharing
     */
    LimitSharing add(Limit limit, TransportTypeEnum transportType, Employee author, BigDecimal sum, BigDecimal balance);
    
    /**
     * Find or create limit sharing.
     *
     * @param limit limit.
     * @param transportType transport type.
     * @param author author.
     *
     * @return limit sharing
     */
    LimitSharing getLimitSharing(Limit limit, TransportTypeEnum transportType, Employee author);
    
    /**
     * Save limit sharing.
     *
     * @param limitSharing limit sharing.
     */
    void save(LimitSharing limitSharing);
    
    /**
     * Delete limit sharing.
     *
     * @param limitSharing limit sharing.
     */
    void delete(LimitSharing limitSharing);
    
    /**
     * Get limit sharing.
     *
     * @param id ID of limit sharing.
     *
     * @return limit sharing.
     */
    Optional<LimitSharing> get(UUID id);
    
    /**
     * Get all limit sharings.
     *
     * @return list of limit sharings.
     */
    List<LimitSharing> getAll();

    Page<LimitSharing> getAll(Integer page, Integer size, Sort.Direction direction, UUID limitId);
    
    /**
     * Distrubute sharing per periods.
     *
     * @param limitSharing limit sharing.
     */
    void distributeSharingPerPeriod(LimitSharing limitSharing);
    
    /**
     * Distrubute sharing per periods imitation
     *
     * @param limit limit.
     * @param totalSum sum for distribution
     */
    Map<Period, BigDecimal> distributeSharingPerPeriodImitation(Limit limit, BigDecimal totalSum);
    
    /**
     * Get by limit.
     *
     * @param limit limit.
     *
     * @return list of limit sharings
     */
    List<LimitSharing> getByLimit(Limit limit);
    
    /**
     * Get by limit ids.
     *
     * @param limitIds limit ids.
     *
     * @return list of limit sharings
     */
    List<LimitSharing> getByLimitIds(List<UUID> limitIds);
    
    /**
     * Get by limit and transport type.
     *
     * @param limit limit.
     *
     * @return limit sharing
     */
    LimitSharing getByLimitAndTransportType(Limit limit, TransportTypeEnum transportType);
    
    /**
     * Move sum to next period.
     *
     * @param limitSharing limit sharing.
     * @param period period.
     */
    void moveRemainsToNextPeriod(LimitSharing limitSharing, Period period);
    
    /**
     * Move sum to next period.
     *
     * @param limitSharing limit sharing.
     * @param period period.
     */
    void moveRemainsToEconomy(LimitSharing limitSharing, Period period);
    
    /**
     * Change limit sharing per period
     *
     * @param limitSharing limit sharing.
     * @param sum sum.
     */
    void changeLimitSharingSumAndBalance(LimitSharing limitSharing, BigDecimal sum);
    
    /**
     * Put sum to next certain month.
     *
     * @param limitSharing limit sharing
     * @param sum sum
     * @param period period
     */
    LimitSharingPerPeriod putToMonth(LimitSharing limitSharing, BigDecimal sum, Period period);
    
    /**
     * Create limit sharing per period
     *
     * @param limitSharing limit sharing.
     * @param author author.
     */
    void createAndDistributeSharingPerPeriod(LimitSharing limitSharing, Employee author);
}
