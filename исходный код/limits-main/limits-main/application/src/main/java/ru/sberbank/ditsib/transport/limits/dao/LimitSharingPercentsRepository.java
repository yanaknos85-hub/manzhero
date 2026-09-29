package ru.sberbank.ditsib.transport.limits.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPercents;

import java.util.Optional;
import java.util.UUID;

/**
 * LimitSharingPercents repository
 */
@Repository
@Transactional(readOnly = true)
public interface LimitSharingPercentsRepository extends JpaRepository<LimitSharingPercents, UUID>, JpaSpecificationExecutor<LimitSharingPercents> {
    
    /**
     * Find limitsharingpercents by limit.
     *
     * @param limit limit.
     *
     * @return limitsharingpercents object.
     */
    Optional<LimitSharingPercents> findByLimit(Limit limit);
}
