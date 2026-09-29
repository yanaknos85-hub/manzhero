package ru.sber.transport.tariff_fleet.service;

import java.util.UUID;

/**
 * Сервис для генерации человекочитаемых идентификаторов
 */
public interface HumanReadableIdService {
    
    /**
     * Создаем человекочитаемый идентификатор по данным организации
     * @param userId Идентификатор записи с таблицы corporate.user
     * @return человекочитаемый идентификатор
     */
    String createHumanReadableIdByUserId(UUID userId);

    /**
     * Создаем человекочитаемый идентификатор по данным организации
     * @param digitId Уникальный идентификатор (числовой)
     * @return человекочитаемый идентификатор
     */
    String createHumanReadableIdByDigitId(Long digitId);
}