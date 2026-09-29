package ru.sberbank.ditsib.transport.limits.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitTransferHistoryType;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitTransferHistory;
import ru.sberbank.ditsib.transport.limits.model.limit.PeriodData;

import java.util.List;
import java.util.UUID;

/**
 * Limit transfer history repository
 */
@Repository
@Transactional(readOnly = true)
public interface LimitTransferHistoryRepository extends JpaRepository<LimitTransferHistory, UUID>, JpaSpecificationExecutor<LimitTransferHistory> {
    
    /**
     * Find by year.
     *
     * @param year year.
     *
     * @return list of LimitTransferHistory objects.
     */
    List<LimitTransferHistory> findByYearAndSourceLimitIdOrderByCreationTime(Integer year, UUID limitId);
    
    /**
     * Find by year.
     *
     * @param year year.
     *
     * @return list of LimitTransferHistory objects.
     */
    List<LimitTransferHistory> findByYearAndTargetLimitIdOrderByCreationTime(Integer year, UUID limitId);
    
    /**
     * Find by year.
     *
     * @param year year.
     *
     * @return list of LimitTransferHistory objects.
     */
    List<LimitTransferHistory> findByYearAndSourceLimitIdAndSourceTransportTypeOrderByCreationTime(Integer year,
                                                                                        UUID sourceLimitId,
                                                                                        TransportTypeEnum transportType);
    
    /**
     * Find by year.
     *
     * @param year year.
     *
     * @return list of LimitTransferHistory objects.
     */
    List<LimitTransferHistory> findByYearAndTargetLimitIdAndTargetTransportTypeOrderByCreationTime(Integer year,
                                                                                                 UUID sourceLimitId,
                                                                                                 TransportTypeEnum transportType);
    
    /**
     * Find by year.
     *
     * @param year year.
     *
     * @return list of LimitTransferHistory objects.
     */
    List<LimitTransferHistory> findByYearAndSourceLimitIdAndSourceTransportTypeAndSourcePeriodOrderByCreationTime(Integer year,
                                                                                                          UUID sourceLimitId,
                                                                                                          TransportTypeEnum transportType,
                                                                                                          String sourcePeriod);
    
    /**
     * Find by year.
     *
     * @param year year.
     *
     * @return list of LimitTransferHistory objects.
     */
    List<LimitTransferHistory> findByYearAndTargetLimitIdAndTargetTransportTypeAndTargetPeriodOrderByCreationTime(Integer year,
                                                                                                          UUID sourceLimitId,
                                                                                                          TransportTypeEnum transportType,
                                                                                                          String period);
    
    /**
     * Find by year.
     *
     * @param year year.
     *
     * @return list of LimitTransferHistory objects.
     */
    @Query("SELECT lth FROM LimitTransferHistory lth where (lth.sourceLimitId = :limitId or lth.targetLimitId = :limitId) " +
            "and lth.year = :year and lth.historyType = :historyType")
    List<LimitTransferHistory> findByYearAndHistoryType(UUID limitId, Integer year, LimitTransferHistoryType historyType);
    
    /**
     * Find by year and period (only economy has period).
     *
     * @param year year.
     * @param sourcePeriod period.
     *
     * @return list of LimitTransferHistory objects.
     */
    List<LimitTransferHistory> findByOrganizationIdAndLimitServiceTypeAndYearAndSourcePeriodAndHistoryType(UUID organizationId, String serviceType,
                                                                                                           Integer year, PeriodData sourcePeriod,
                                                                                                           LimitTransferHistoryType historyType);
}
