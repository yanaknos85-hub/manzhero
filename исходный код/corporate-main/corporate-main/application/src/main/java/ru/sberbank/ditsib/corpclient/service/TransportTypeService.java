package ru.sberbank.ditsib.corpclient.service;

import ru.sberbank.ditsib.corpclient.database.model.TransportType;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with transport types.
 */
public interface TransportTypeService {
    
    /**
     * Get transport type.
     *
     * @param id ID of type.
     *
     * @return transport type.
     */
    Optional<TransportType> get(UUID id);
    
    /**
     * Delete transport type.
     *
     * @param type type to delete.
     */
    void delete(TransportType type);
    
    /**
     * Save transport type.
     *
     * @param type type to save.
     */
    void save(TransportType type);
}
