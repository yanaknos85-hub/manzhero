package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.PublicTrApprovalsSettings;

import java.util.Optional;
import java.util.UUID;

public interface PublicTrApprovalsSettingsRepository extends JpaRepository<PublicTrApprovalsSettings, UUID> {
    
    Optional<PublicTrApprovalsSettings> findByOrganizationId(UUID organizationId);

}
