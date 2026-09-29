package ru.sber.transport.tariff_fleet.service;


import ru.sber.transport.tariff_fleet.dto.GetAllActiveOrganizationNamesDto;

import java.util.List;
import java.util.UUID;

/**
 * Service for working with fleet owner organizations.
 */
public interface FleetOwnerOrganizationService {
    /**
     * Проверка наличия записи в базе данных
     *
     * @param id Идентификатор записи о подразделении
     *
     * @return наличие записи в базе данных
     */
    boolean existsById(UUID id);
    
    /**
     * Получение списка активных организаций владельцев автопарков
     *
     * @return {@link List<GetAllActiveOrganizationNamesDto>}
     */
    List<GetAllActiveOrganizationNamesDto> getAllActive();
}
