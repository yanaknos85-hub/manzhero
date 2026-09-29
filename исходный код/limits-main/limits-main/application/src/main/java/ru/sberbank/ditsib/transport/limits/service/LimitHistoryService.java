package ru.sberbank.ditsib.transport.limits.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitHistoryType;
import ru.sberbank.ditsib.transport.limits.model.LimitData;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitHistory;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharing;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPerPeriod;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Service for working with LimitSpending objects.
 */
public interface LimitHistoryService {
    
    /**
     * Add limit transfer history.
     *  @param limitData data about limit.
     * @param counterLimitData data about counter-limit.
     * @param year year.
     * @param historyType historyType.
     * @param sum sum.
     * @param authorId author id.
     * @param limitSharingPerPeriod sharing per period.
     * @return history data.
     */
    Future<LimitHistory> add(
            UUID authorId,
            LimitData limitData,
            LimitData counterLimitData,
            BigDecimal sum, Integer year,
            LimitHistoryType historyType,
            LimitSharing limitSharing,
            LimitSharingPerPeriod limitSharingPerPeriod);
    
    /**
     * Save limit transfer history.
     *
     * @param limitHistory limit transfer history.
     * @return history data.
     */
    LimitHistory save(LimitHistory limitHistory);
    
    /**
     * Delete limit transfer history.
     *
     * @param limitHistory limit transfer history.
     */
    void delete(LimitHistory limitHistory);
    
    /**
     * Get transfer history.
     *
     * @param id ID of transfer history.
     *
     * @return limit transfer history.
     */
    Optional<LimitHistory> get(UUID id);
    
    /**
     * Get all limit transfer history.
     *
     * @return list of limit transfer history.
     */
    List<LimitHistory> getAll();

    /**
     * Get all limit transfer history.
     *
     * @return list of limit transfer history.
     */
    Page<LimitHistory> getAll(UUID limitId, int page, int size, Sort.Direction direction);
    
    /**
     * Get all limit transfer history by source or target limit.
     *
     * @return list of limit transfer history.
     */
    List<LimitHistory> getByLimit(UUID limitId);
    
    /**
     * Get all limit transfer history by source or target limit.
     *
     * @return list of limit transfer history.
     */
    List<LimitHistory> getByLimitAndTransportType(UUID limitId, TransportTypeEnum transportType);
    
    /**
     * Get all limit transfer history by source or target limit.
     *
     * @return list of limit transfer history.
     */
    List<LimitHistory> getByLimitAndTransportTypeAndPeriod(UUID limitId, TransportTypeEnum transportType, Period period);
}
