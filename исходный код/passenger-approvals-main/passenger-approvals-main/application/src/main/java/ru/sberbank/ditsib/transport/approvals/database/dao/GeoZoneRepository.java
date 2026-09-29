package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.GeoZone;

import java.util.UUID;

/**
 * Репозиторий для работы с геозонами.
 */
public interface GeoZoneRepository extends JpaRepository<GeoZone, UUID> {
}
