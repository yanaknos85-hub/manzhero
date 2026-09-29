package ru.sberbank.ditsib.corpclient.service;

import ru.sberbank.ditsib.corpclient.database.model.CargoType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Service for working with cargo types.
 */
public interface CargoTypeService {

    /**
     * Get status type by id
     *
     * @param uuid id.
     * @return type
     */
    CargoType getActive(UUID uuid);

    /**
     * Get status type by id and organization
     *
     * @param id id.
     * @param organizationId organization.
     * @return type
     */
    CargoType getActiveByOrganizationId(UUID id, UUID organizationId);

    /**
     * Get all status types by organization
     *
     * @param organizationId organization.
     * @return types
     */
    List<CargoType> getAllActiveByOrganizationId(UUID organizationId);

    /**
     * Get all status types
     *
     * @return all types
     */
    List<CargoType> getAllActive();

    CargoType save(CargoType cargoType);

    /**
     * Deactivation type.
     *
     * @param uuid ID of type.
     */
    void deActivation(@NotNull UUID uuid);

    CargoType getByName(String name);

    /**
     * Search for types.
     *
     * @param searchText search string.
     * @return list of types
     */
    List<CargoType> search(@NotBlank String searchText);

    /**
     * Search for types with organizationId = null.
     *
     * @param searchText search string.
     * @return list of types
     */
    List<CargoType> searchWithEmptyOrganization(String searchText);

    /**
     * Search for types by organization.
     *
     * @param searchText search string.
     * @param organizationId organization id.
     * @return list of types
     */
    List<CargoType> search(UUID organizationId, String searchText);


}
