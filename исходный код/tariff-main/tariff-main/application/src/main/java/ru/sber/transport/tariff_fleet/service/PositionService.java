package ru.sber.transport.tariff_fleet.service;


import ru.sber.transport.tariff_fleet.database.model.Position;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with positions.
 */
public interface PositionService {
    
    Optional<Position> get(UUID id);
    
    /**
     * Delete position.
     *
     * @param entity position to delete.
     */
    void delete(Position entity);
    
    void saveOrUpdate(Position entity);
}
