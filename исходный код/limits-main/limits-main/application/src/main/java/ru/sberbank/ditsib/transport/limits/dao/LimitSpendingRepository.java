package ru.sberbank.ditsib.transport.limits.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.limits.constants.LimitSpendingStatus;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPerPeriod;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSpending;

import java.util.List;
import java.util.UUID;

/**
 * Repository for working with limits spending objects.
 */
@Repository
@Transactional(readOnly = true)
public interface LimitSpendingRepository extends JpaRepository<LimitSpending, UUID>, JpaSpecificationExecutor<LimitSpending> {
    
    /**
     * Find limitspending by request id.
     *
     * @param requestId request id.
     *
     * @return list of LimitSharingPerPeriod objects.
     */
    List<LimitSpending> findByRequestId(UUID requestId);
    
    /**
     * Find limitspending by LimitSharingPerPeriod.
     *
     * @param limitSharingPerPeriod limitSharingPerPeriod.
     *
     * @return list of LimitSharingPerPeriod objects.
     */
    List<LimitSpending> findByLimitSharingPerPeriod(LimitSharingPerPeriod limitSharingPerPeriod);
    
    /**
     * Find limitspending by limitId and year.
     *
     * @param limitId limitId
     *
     * @return list of LimitSharingPerPeriod objects.
     */
    @Query("SELECT limitSpending FROM LimitSpending limitSpending " +
           "INNER JOIN limitSpending.limitSharingPerPeriod limitSharingPerPeriod " +
           "INNER JOIN limitSharingPerPeriod.limitSharing limitSharing " +
           "INNER JOIN limitSharing.limit limit " +
           "WHERE limit.id = :limitId " +
           "ORDER BY limitSpending.spendingTime DESC")
    List<LimitSpending> findByLimit(UUID limitId);
}
