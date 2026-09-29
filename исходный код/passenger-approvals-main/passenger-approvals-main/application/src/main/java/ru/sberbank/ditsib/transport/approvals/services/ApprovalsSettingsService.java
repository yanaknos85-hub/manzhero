package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.database.model.ApprovalsSettings;

import java.util.UUID;

/**
 * Сервис для работы с настройками согласования такси и общественного транспорта
 *
 * @param <T> тип настройки согласования
 */
public interface ApprovalsSettingsService<T extends ApprovalsSettings> {
    
    /**
     * Создать новую настройку согласования
     * @param organizationId - идентификатор организации
     * @param settingsDto - модель новой настройки согласования
     * @return созданная настройка согласования
     */
    T add(UUID organizationId, T settingsDto);
    
    /**
     * Получить настройку согласования для ораганизации
     * @param organizationId - идентификатор организации
     * @return настройка согласования
     */
    T get(UUID organizationId);
    
    /**
     * Обновить настройку согласования
     * @param organizationId - идентификатор организации
     * @param settingId - идентификатор настройки
     * @param settingsDTO - новая модель настройки согласования
     */
    void update(UUID organizationId, UUID settingId, T settingsDTO);
    
    /**
     * Удалить настройку согласования
     * @param organizationId - идентификатор организации
     * @param settingId - идентификатор настройки
     */
    void delete(UUID organizationId, UUID settingId);
    
    /**
     * Восстановить значения настройки по умолчанию
     * @param organizationId - идентификатор организации
     * @param settingId - идентификатор настройки
     * @return новая модель настройки согласования
     */
    T restoreValues(UUID organizationId, UUID settingId);
}
