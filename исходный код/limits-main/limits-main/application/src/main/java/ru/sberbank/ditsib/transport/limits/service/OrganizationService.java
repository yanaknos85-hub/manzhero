package ru.sberbank.ditsib.transport.limits.service;

import ru.sberbank.ditsib.transport.limits.model.basic.Organization;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with organizations.
 */
public interface OrganizationService {
    
    /**
     * Get organization.
     *
     * @param id ID of organization.
     *
     * @return organization.
     */
    Optional<Organization> get(UUID id);
    
    /**
     * Get list of active organizations
     *
     * @return organization.
     */
    List<Organization> getAllActive();
    
    /**
     * Delete organization.
     *
     * @param organization organization to delete.
     */
    void delete(Organization organization);
    
    /**
     * Save organization.
     *
     * @param organization organization to save.
     */
    Organization save(Organization organization);
}
