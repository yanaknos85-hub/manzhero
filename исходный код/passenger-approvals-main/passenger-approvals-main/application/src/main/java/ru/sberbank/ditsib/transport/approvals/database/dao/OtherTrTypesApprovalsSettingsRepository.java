package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.OtherTrTypesApprovalsSettings;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с настройками согласования всех типов транспорта, кроме такси и общественного
 */
public interface OtherTrTypesApprovalsSettingsRepository extends JpaRepository<OtherTrTypesApprovalsSettings, UUID> {
    
    Optional<OtherTrTypesApprovalsSettings> findByOrganizationIdAndTransportType(UUID organizationId,
                                                                                 String transportType);
    
    List<OtherTrTypesApprovalsSettings> findAllByOrganizationId(UUID organizationId);
}
