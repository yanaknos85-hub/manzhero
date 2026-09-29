package ru.sberbank.ditsib.transport.limits.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitTransferHistoryType;
import ru.sberbank.ditsib.transport.limits.model.LimitData;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitTransferHistory;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Service for working with LimitSpending objects.
 */
public interface LimitTransferHistoryService {
    
    /**
     * Добавить запись в историю.
     *  @param authorId идентификатор автора.
     * @param source исходные данные.
     * @param target целевые данные.
     * @param sum sum.
     * @param year год.
     * @param historyType тип истории.
     * @return
     */
    Future<LimitTransferHistory> add(
            UUID authorId, LimitData source, LimitData target, BigDecimal sum,
            Integer year, LimitTransferHistoryType historyType);
    
    /**
     * Save limit transfer history.
     *
     * @param limitTransferHistory limit transfer history.
     */
    LimitTransferHistory save(LimitTransferHistory limitTransferHistory);
    
    /**
     * Delete limit transfer history.
     *
     * @param limitTransferHistory limit transfer history.
     */
    void delete(LimitTransferHistory limitTransferHistory);
    
    /**
     * Get transfer history.
     *
     * @param id ID of transfer history.
     *
     * @return limit transfer history.
     */
    Optional<LimitTransferHistory> get(UUID id);
    
    /**
     * Get all limit transfer history.
     *
     * @return list of limit transfer history.
     */
    List<LimitTransferHistory> getAll();
    
    /**
     * Get all limit transfer history by source or target limit.
     *
     * @return list of limit transfer history.
     */
    List<LimitTransferHistory> getByLimit(Integer year, UUID limitId);
    
    /**
     * Get all limit transfer history by source or target limit.
     *
     * @return list of limit transfer history.
     */
    List<LimitTransferHistory> getByLimitAndTransportType(Integer year, UUID limitId, TransportTypeEnum transportType);
    
    /**
     * Get all limit transfer history by source or target limit.
     *
     * @return list of limit transfer history.
     */
    List<LimitTransferHistory> getByLimitAndTransportTypeAndPeriod(Integer year, UUID limitId,
                                                                   TransportTypeEnum transportType, Period period);
    
    /**
     * Get all limit transfer history by source limit.
     *
     * @return list of limit transfer history.
     */
    List<LimitTransferHistory> getBySourceLimit(Integer year, UUID limitId);
    
    /**
     * Get all limit transfer history by target limit.
     *
     * @return list of limit transfer history.
     */
    List<LimitTransferHistory> getByTargetLimit(Integer year, UUID limitId);
    
    /**
     * Find by year and period (only economy has period).
     *
     * @param year year.
     * @param period period.
     *
     * @return list of LimitTransferHistory objects.
     */
    List<LimitTransferHistory> getHistoryTransfersToEconomy(UUID organizationId, String serviceType,
                                                            Integer year, Period period);
    
    
    
    /**
     * Find by year and period (only economy has period).
     *
     * @param year year.
     *
     * @return list of LimitTransferHistory objects.
     */
    List<LimitTransferHistory> getHistoryTransfersFromEconomy(UUID limitId, Integer year);

    Page<LimitTransferHistory> getAll(UUID organizationId, UUID limitId, Integer year, int page, int size, Sort.Direction direction);
}
