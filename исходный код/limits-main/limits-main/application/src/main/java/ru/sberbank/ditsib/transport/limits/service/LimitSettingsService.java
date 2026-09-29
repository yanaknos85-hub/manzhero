package ru.sberbank.ditsib.transport.limits.service;

import ru.sberbank.ditsib.transport.limits.constants.SettingsNames;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSettings;

import java.util.List;

/**
 * Service for working with limit settings.
 */
public interface LimitSettingsService {

    /**
     * Add limit setting.
     *
     * @param name name.
     * @param value value.
     */
    LimitSettings add(SettingsNames name, Object value);
    
    /**
     * Save limit setting.
     *
     * @param limitSettings limit setting.
     */
    LimitSettings save(LimitSettings limitSettings);
    
    /**
     * Delete limit setting.
     *
     * @param name limit setting.
     */
    void delete(SettingsNames name);
    
    /**
     * Get limitSpending.
     *
     * @param name name of limitSpending.
     *
     * @return limit setting.
     */
    LimitSettings get(SettingsNames name);
    
    /**
     * Get limitSpending.
     *
     * @param name name of limitSpending.
     *
     * @return limit setting.
     */
    String getByName(SettingsNames name);
    
    /**
     * Get all limit settings.
     *
     *
     * @return list of limit setting.
     */
    List<LimitSettings> getAll();
}
