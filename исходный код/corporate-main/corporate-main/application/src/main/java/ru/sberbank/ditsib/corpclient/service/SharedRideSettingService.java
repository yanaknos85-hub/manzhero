package ru.sberbank.ditsib.corpclient.service;

import ru.sberbank.ditsib.corpclient.dto.SharedRideSettingsCreateDTO;
import ru.sberbank.ditsib.corpclient.dto.SharedRideSettingsUpdateDTO;

import java.util.List;
import java.util.UUID;

/**
 * CRUD-сервис настройки совместных поездок
 */
public interface SharedRideSettingService {
    
    /**
     * Получить список всех настроек совместных поездок для организации
     * @param organizationId идентификатор организации
     * @return коллекция DTO настроек совместных поездок
     */
    List<SharedRideSettingsUpdateDTO> getAllSettingsByOrganizationId(UUID organizationId);
    
    /**
     * Получить конкретную настройку совместных поездок для организации
     * @param organizationId идентификатор организации
     * @param settingsId идентификатор настройки
     * @return DTO настройки совместных поездок
     */
    SharedRideSettingsUpdateDTO getSingleSettingsByOrganizationIdAndSettingId(
            UUID organizationId,
            UUID settingsId
    );
    
    /**
     * Сохранить новую настройку совместных поездок для организации
     * @param organizationId идентификатор организации
     * @param newSettingsDto данные новых настроек
     * @return DTO настройки совместных поездок
     */
    SharedRideSettingsUpdateDTO saveNewSettings(
            UUID organizationId,
            SharedRideSettingsCreateDTO newSettingsDto
    );
    
    /**
     * Редактировать имеющуюся настройку совместных поездок
     * @param organizationId идентификатор организации
     * @param updateSettingsDto данные редактируемой настройки
     * @return измененная настройка
     */
    SharedRideSettingsUpdateDTO updateSettings(
            UUID organizationId,
            SharedRideSettingsUpdateDTO updateSettingsDto
    );
    
    /**
     * Удалить имеющуюся настройку совместных поездок
     * @param organizationId идентификатор организации
     * @param settingsId идентификатор настройки
     */
    void deleteSettings(UUID organizationId, UUID settingsId);
}
