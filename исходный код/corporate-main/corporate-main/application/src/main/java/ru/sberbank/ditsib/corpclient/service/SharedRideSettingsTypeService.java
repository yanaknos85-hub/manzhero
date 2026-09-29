package ru.sberbank.ditsib.corpclient.service;

import ru.sberbank.ditsib.corpclient.database.model.SharedRideSettingType;

import java.util.Set;

/**
 * Cервис для выдачи типов настройки совместных поездок
 */
public interface SharedRideSettingsTypeService {
    /**
     * Получение всех типов настроек совместных поездок
     * @return все типы настроек
     */
    Set<SharedRideSettingType> getAllSettingsTypes();
}
