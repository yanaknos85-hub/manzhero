package ru.sberbank.ditsib.transport.limits.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharing;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for working with LimitSharing objects.
 */
@Repository
@Transactional(readOnly = true)
public interface LimitSharingRepository extends JpaRepository<LimitSharing, UUID>, JpaSpecificationExecutor<LimitSharing> {
    
    /**
     * Find limitsharing by limit.
     *
     * @param limit limit.
     *
     * @return list of limitsharing objects.
     */
    List<LimitSharing> findByLimit(Limit limit);
    
    /**
     * Find limitsharings by limit ids.
     *
     * @param ids limit ids.
     *
     * @return list of limitsharing objects.
     */
    List<LimitSharing> findByLimitIdIn(Collection<UUID> ids);
    
    /**
     * Find limitsharingpercents by limit and transport type.
     *
     * @param limit limit
     * @param transportType transport type
     *
     * @return list of limitsharing objects.
     */
    Optional<LimitSharing> findByLimitAndTransportType(Limit limit, TransportTypeEnum transportType);
}
