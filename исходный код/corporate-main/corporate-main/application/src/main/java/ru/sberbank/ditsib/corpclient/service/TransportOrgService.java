package ru.sberbank.ditsib.corpclient.service;

import ru.sberbank.ditsib.corpclient.database.model.TransportOrg;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service of transport organizations links.
 */
public interface TransportOrgService {
    
    /**
     * Delete transport type.
     *
     * @param transportType type to delete.
     * @param organizationId ID of organization.
     */
    void delete(UUID organizationId, String transportType);
    
    /**
     * Save transport type.
     *
     * @param transportType type to delete.
     * @param organizationId ID of organization.
     */
    void save(UUID organizationId, String transportType);
    
    /**
     * Get transport type.
     *
     * @param organizationId ID of organization.
     *
     * @return list of transport types.
     */
    List<String> getByOrganizationId(UUID organizationId);

    /**
     * Get transport organization.
     *
     * @param transportType type to delete.
     * @param organizationId ID of organization.
     *
     * @return transport organization.
     */
    Optional<TransportOrg> getByOrganizationIdAndTransportType(UUID organizationId, String transportType);
}
