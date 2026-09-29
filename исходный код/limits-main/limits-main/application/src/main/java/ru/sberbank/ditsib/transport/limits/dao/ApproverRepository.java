package ru.sberbank.ditsib.transport.limits.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.limits.model.limit.Approver;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitRequest;

import java.util.UUID;

/**
 * Repository for working with limits.
 */
@Repository
public interface ApproverRepository extends JpaRepository<Approver, UUID>,
                                            JpaSpecificationExecutor<LimitRequest> {
}