package ru.sberbank.ditsib.transport.limits.service;

import ru.sberbank.ditsib.transport.limits.model.deadline.DeadlineSettings;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с настройками КС
 */
public interface DeadlineSettingsService {
    
    /**
     * Get deadline settings by ID.
     * @param settingsId ID of settings.
     * @return get deadline settings.
     */
    Optional<DeadlineSettings> getOptional(UUID settingsId);
    
    /**
     * Создать или отредактировать настройки КС
     * @param settings данные DeadlineSettings
     * @return сохраненные DeadlineSettings
     */
    DeadlineSettings save(DeadlineSettings settings);
    
    /**
     * Удаление настроек КС
     * @param settings существующие DeadlineSettings
     */
    void delete(DeadlineSettings settings);
}
