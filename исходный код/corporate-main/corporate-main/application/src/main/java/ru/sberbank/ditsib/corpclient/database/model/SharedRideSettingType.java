package ru.sberbank.ditsib.corpclient.database.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Тип Настройки совместных поездок
 */
@Schema(title = "Тип Настройки совместных поездок")
public enum SharedRideSettingType {

    /**
     * Индивидуальная поездка не доступна
     */
    INDIVIDUAL_RIDE_IS_NOT_AVAILABLE,
    
    /**
     * Может ездить только индивидуально
     */
    CAN_RIDE_INDIVIDUALLY_ONLY,
    
    /**
     * Согласование присоединения к СП
     */
    CONFIRMATION_OF_JOIN_THE_SHARED_RIDE
}
