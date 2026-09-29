package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.corpclient.database.model.messages.GeoZone;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с геозонами.
 */
public interface GeoZoneRepository extends JpaRepository<GeoZone, UUID> {
    
    /**
     * Поиск геозоны по имени.
     *
     * @param geoZone имя геозоны.
     * @return геозона.
     */
    Optional<GeoZone> findByName(String geoZone);
    
}
