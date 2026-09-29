package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.TripPurpose;

import java.util.UUID;

/**
 * Repository of purpose
 */
public interface TripPurposeRepository extends JpaRepository<TripPurpose, UUID> {
}
