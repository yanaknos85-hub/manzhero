package ru.sberbank.ditsib.transport.limits.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.limits.model.deadline.DeadlineSettings;

import java.util.Optional;
import java.util.UUID;

public interface DeadlineSettingsRepository extends JpaRepository<DeadlineSettings, UUID> {
    
    Optional<DeadlineSettings> findByOrganizationId(UUID organizationId);
}
