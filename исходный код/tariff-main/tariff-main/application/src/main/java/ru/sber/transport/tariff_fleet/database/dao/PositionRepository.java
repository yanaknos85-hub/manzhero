package ru.sber.transport.tariff_fleet.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.tariff_fleet.database.model.Position;

import java.util.UUID;

/**
 * Repository of positions
 */
public interface PositionRepository extends JpaRepository<Position, UUID> {
}
