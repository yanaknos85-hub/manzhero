package ru.sberbank.ditsib.transport.limits.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.limits.model.LimitCompliance;

import java.util.UUID;

@Repository
public interface LimitComplianceRepository extends JpaRepository<LimitCompliance, UUID> {
}
