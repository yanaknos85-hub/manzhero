package ru.sberbank.ditsib.corpclient.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.dto.OrganizationField;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Organization crud and other operations
 */
public interface OrganizationService {
    
    /**
     * @param id identifier of organization
     *
     * @return found organization
     */
    Organization get(@NotNull UUID id);
    
    /**
     * @param name name of organization
     *
     * @return found organization
     */
    Optional<Organization> get(@NotNull String name);
    
    /**
     * Получение всех подразделений.
     *
     * @return подразделения.
     */
    List<Organization> getAll();
    
    /**
     * @param id identifier of organization to delete
     */
    void delete(@NotNull UUID id);
    
    /**
     * Get all organizations.
     *
     * @return collection of employees.
     */
    List<Organization> get(Sort sort, Map<OrganizationField, Serializable> filter);

    /**
     * Get all organizations.
     *
     * @return collection of employees.
     */
    Page<Organization> get(int page, int size, Sort sort, Map<OrganizationField, Serializable> filter);

    /**
     * Добавить организацию.
     *
     * @param model данные новой организации.
     * @return добавленая организация.
     * @deprecated since 3.24 Используйте {@link ru.sber.transport.corporate.business.providers.OrganizationProvider#save(ru.sber.transport.corporate.business.model.Organization)}
     */
    @Deprecated(since = "3.24")
    Organization add(Organization model);
    
    /**
     * Изменить организацию.
     *
     * @param organizationId идентификатор организации.
     * @param newData новые данные организации.
     */
    void edit(UUID organizationId, Organization newData);
    
    /**
     * throws EntityNotFoundResponseException if organization with specified id doesnt exist
     *
     * @param organizationId id of organization to find
     *
     * @return true if organization exists or throws exception
     */
    boolean validateOrganizationId(@NotNull UUID organizationId);

}