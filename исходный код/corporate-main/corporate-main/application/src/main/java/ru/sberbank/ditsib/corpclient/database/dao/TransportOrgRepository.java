package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.corpclient.database.model.TransportOrg;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransportOrgRepository extends JpaRepository<TransportOrg, UUID> {

    List<TransportOrg> getByOrganizationId(UUID organizationId);

    Optional<TransportOrg> getByOrganizationIdAndTransportType(UUID organizationId, String transportType);

    /**
     * Получение списка типов транспорта организации
     *
     * @param list список организаций
     * @return список типов транспорта
     */
    List<TransportOrg> getAllByOrganizationIdIn(List<UUID> list);
}
