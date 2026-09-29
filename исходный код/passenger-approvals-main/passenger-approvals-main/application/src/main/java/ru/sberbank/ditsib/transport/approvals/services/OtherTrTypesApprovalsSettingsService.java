package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.database.model.OtherTrTypesApprovalsSettings;

import java.util.UUID;

/**
 * Сервис для работы с настройками согласования транспорта, кроме такси и общественного
 */
public interface OtherTrTypesApprovalsSettingsService {
    
    /**
     * Создать новую настройку согласования
     * @param organizationId - идентификатор организации
     * @param settingsDto - модель новой настройки согласования
     * @return созданная настройка согласования
     */
    OtherTrTypesApprovalsSettings add(UUID organizationId, OtherTrTypesApprovalsSettings settingsDto);
    
    /**
     * Получить настройку согласования для ораганизации
     * @param organizationId - идентификатор организации
     * @param settingId - идентификатор настройки
     * @return настройка согласования
     */
    OtherTrTypesApprovalsSettings get(UUID organizationId, String settingId);
    
    /**
     * Обновить настройку согласования
     * @param organizationId - идентификатор организации
     * @param transportType - тип транспорта
     * @param settingsDTO - новая модель настройки согласования
     */
    void update(UUID organizationId, String transportType, OtherTrTypesApprovalsSettings settingsDTO);
    
    /**
     * Удалить настройку согласования
     * @param organizationId - идентификатор организации
     * @param transportType - тип транспорта
     */
    void delete(UUID organizationId, String transportType);
    
    /**
     * Восстановить значения настройки по умолчанию
     * @param organizationId - идентификатор организации
     * @param transportType - тип транспорта
     * @return новая модель настройки согласования
     */
    OtherTrTypesApprovalsSettings restoreValues(UUID organizationId, String transportType);
}
