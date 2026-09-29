package ru.sber.transport.tariff_fleet.service;


import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.dto.GetAllActiveOrganizationNamesDto;
import ru.sber.transport.tariff_fleet.dto.GetDepartmentsInfo;
import ru.sber.transport.tariff_fleet.dto.OrganizationsDepartmentsSearchDto;

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
     * Delete organization.
     *
     * @param entity organization to delete.
     */
    void delete(Organization entity);
    
    void saveOrUpdate(Organization entity);
    
    /**
     * Get all active organization names
     *
     * @return List of organizations ids and names
     *
     * @link GetAllActiveOrganizationNamesDto
     */
    List<GetAllActiveOrganizationNamesDto> getAllActiveNames();
    
    /**
     * Get all departments info by organization ids
     *
     * @param dto with list of organization ids
     *
     * @return List of departments info
     */
    List<GetDepartmentsInfo> getDepartmentsInfo(OrganizationsDepartmentsSearchDto dto);
    
    /**
     * Получение уникального идентификатора (числового) организации по идентификатору организации
     *
     * @param userId Идентификатор записи с таблицы corporate.user
     *
     * @return Уникальный идентификатор (числовой)
     */
    Long getDigitIdByUserId(UUID userId);
    
    /**
     * Проверка наличия записи в базе данных
     *
     * @param id Идентификатор записи о подразделении
     * @return наличие записи в базе данных
     */
    boolean existsById(UUID id);
}
