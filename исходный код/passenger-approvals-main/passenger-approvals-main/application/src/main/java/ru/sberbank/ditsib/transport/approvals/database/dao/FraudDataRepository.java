package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.approvals.database.model.FraudData;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FraudDataRepository extends JpaRepository<FraudData, UUID> {

    void deleteByRequestId(UUID requestId);

    List<FraudData> findAllByApprovalIdIn(Set<UUID> ids);
}
