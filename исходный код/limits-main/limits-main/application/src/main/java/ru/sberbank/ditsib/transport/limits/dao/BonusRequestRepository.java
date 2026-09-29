package ru.sberbank.ditsib.transport.limits.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.limits.model.bonus.BonusRequest;

import java.util.Optional;
import java.util.UUID;

public interface BonusRequestRepository extends JpaRepository<BonusRequest, UUID> {
    
    Optional<BonusRequest> findByRequestId(UUID requestId);
}