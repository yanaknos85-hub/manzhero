package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.TaxiApprovalsSettings;

import java.util.Optional;
import java.util.UUID;

public interface TaxiApprovalsSettingsRepository extends JpaRepository<TaxiApprovalsSettings, UUID> {
    
    Optional<TaxiApprovalsSettings> findByOrganizationId(UUID organizationId);

}
