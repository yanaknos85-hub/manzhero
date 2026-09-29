package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.corpclient.database.model.SharedRideSettings;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA репозитарий элементов настроек совместных поездок
 */
@Repository
public interface SharedRideSettingsRepository extends JpaRepository<SharedRideSettings, UUID> {
    
    List<SharedRideSettings> findByOrganizationId(UUID organizationId);
   
    Optional<SharedRideSettings> findByOrganizationIdAndId(UUID organizationId, UUID settingsId);
   
    Optional<SharedRideSettings> findByOrganizationIdAndTransportType(
            UUID organizationId,
            TransportTypeEnum transportType
    );
}
