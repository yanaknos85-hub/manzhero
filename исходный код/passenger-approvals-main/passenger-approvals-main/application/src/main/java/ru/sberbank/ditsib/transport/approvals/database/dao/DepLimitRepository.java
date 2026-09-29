package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.DepLimit;

import java.util.UUID;

/**
 * Репозиторий для работы с лимитами департамента.
 */
public interface DepLimitRepository extends JpaRepository<DepLimit, UUID> {

}
