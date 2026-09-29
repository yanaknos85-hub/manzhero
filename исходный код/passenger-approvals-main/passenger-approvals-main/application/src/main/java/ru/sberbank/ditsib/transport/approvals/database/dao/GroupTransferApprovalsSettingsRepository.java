package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.GroupTransferApprovalsSettings;

import java.util.Optional;
import java.util.UUID;

public interface GroupTransferApprovalsSettingsRepository extends JpaRepository<GroupTransferApprovalsSettings, UUID> {

    Optional<GroupTransferApprovalsSettings> findByOrganizationId(UUID organizationId);

}
