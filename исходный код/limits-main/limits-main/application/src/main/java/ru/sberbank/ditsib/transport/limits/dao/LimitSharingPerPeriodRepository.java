package ru.sberbank.ditsib.transport.limits.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharing;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPerPeriod;
import ru.sberbank.ditsib.transport.limits.model.limit.PeriodData;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for working with LimitSharingPerPeriod objects.
 */
@Repository
@Transactional(readOnly = true)
public interface LimitSharingPerPeriodRepository extends JpaRepository<LimitSharingPerPeriod, UUID>, JpaSpecificationExecutor<LimitSharingPerPeriod> {
    
    /**
     * Find limitsharingperperiod by limitsharing.
     *
     * @param limitSharing limit sharing.
     *
     * @return list of limitsharingperperiod.
     */
    List<LimitSharingPerPeriod> findByLimitSharing(LimitSharing limitSharing);
    
    /**
     * Find limitsharingperperiod by limitsharing and period number.
     *
     * @param limitSharing limit sharing.
     * @param period period number.
     *
     * @return list of limitsharingperperiod.
     */
    Optional<LimitSharingPerPeriod> findByLimitSharingAndPeriodData(LimitSharing limitSharing, PeriodData period);


    /**
     * Find limitSharingPerPeriod by limitsSharingIds and period data.
     *
     * @param sharingIds limit sharing ids.
     * @param periodData period data.
     *
     * @return list of limitsharingperperiod.
     */
    @Query("""
            SELECT lspp FROM LimitSharingPerPeriod lspp 
            JOIN FETCH lspp.limitSharing ls
            JOIN FETCH ls.limit 
            WHERE ls.id IN :sharingIds 
            AND lspp.periodData = :periodData
            """)
    List<LimitSharingPerPeriod> findByLimitSharingIdsInAndPeriodData(@Param("sharingIds") Collection<UUID> sharingIds, @Param("periodData") PeriodData periodData);

    /**
     * Find limitsharings by limit ids.
     *
     * @param ids limit ids.
     *
     * @return list of limitsharing objects.
     */
    List<LimitSharingPerPeriod> findByLimitSharingIdIn(Collection<UUID> ids);
    
    @Query("SELECT l FROM LimitSharingPerPeriod l WHERE l.periodData = :period AND l.limitSharing.id IN (:limitShardingsIds)")
    List<LimitSharingPerPeriod> findShardingByPeriods(List<UUID> limitShardingsIds, PeriodData period);
}
