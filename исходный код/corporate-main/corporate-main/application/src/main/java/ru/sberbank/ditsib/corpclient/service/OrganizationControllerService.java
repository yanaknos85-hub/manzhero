package ru.sberbank.ditsib.corpclient.service;

import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.dto.*;
import ru.sberbank.ditsib.request.Direction;

import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Сервис контроллера для работы с организациями.
 */
public interface OrganizationControllerService {
    
    /**
     * Изменение данных организации.
     *
     * @param id идентификатор организации на изменение.
     * @param newData новые данные организации.
     */
    void edit(UUID id, Organization newData);
    
    /**
     * Удаление данных организации.
     *
     * @param orgId идентификатор организации.
     */
    void delete(UUID orgId);
    
    /**
     * Получение организации.
     *
     * @param organizationId идентификатор организации.
     * @return организация.
     */
    Organization get(UUID organizationId);
    
    /**
     * Получение всех организаций.
     *
     * @return организации.
     */
    Iterable<OrganizationSelectDTO> get(int page, int size, Direction direction,
                                        OrganizationField field, Map<OrganizationField, Serializable> filter, OrganizationProjection projection);
    
    /**
     * throws EntityNotFoundResponseException if organization with specified id doesnt exist
     *
     * @param organizationId id of organization to find
     *
     * @return true if organization exists or throws exception
     */
    boolean validateOrganizationId(@NotNull UUID organizationId);
}
