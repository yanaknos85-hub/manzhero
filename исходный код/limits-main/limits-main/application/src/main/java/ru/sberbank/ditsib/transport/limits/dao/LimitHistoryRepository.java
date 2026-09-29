package ru.sberbank.ditsib.transport.limits.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitHistory;

import java.util.List;
import java.util.UUID;

/**
 * Limit transfer history repository
 */
@Repository
@Transactional(readOnly = true)
public interface LimitHistoryRepository extends JpaRepository<LimitHistory, UUID>, JpaSpecificationExecutor<LimitHistory> {
    
    /**
     * Find by limitId.
     *
     * @param limitId limitId.
     *
     * @return list of LimitHistory objects.
     */
    List<LimitHistory> findByLimitIdOrderByCreationTime(UUID limitId);
    
    /**
     * Find by limitId.
     *
     * @param limitId limitId.
     *
     * @return list of LimitHistory objects.
     */
    List<LimitHistory> findByLimitIdAndTransportTypeOrderByCreationTime(UUID limitId, TransportTypeEnum transportType);
    
    /**
     * Find limitId limitId.
     *
     * @param limitId limitId.
     *
     * @return list of LimitHistory objects.
     */
    List<LimitHistory> findByLimitIdAndTransportTypeAndPeriodOrderByCreationTime(UUID limitId,
                                                                                 TransportTypeEnum transportType, String period);
    
}
