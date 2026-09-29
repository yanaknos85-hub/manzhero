package ru.sber.transport.corporate.business.providers;

import java.util.Set;
import java.util.UUID;

/**
 * Провайдер допустимых типов транспорта
 */
public interface AvailableClassesProvider {

    /**
     * Получить допустимые типы транспорта
     * @param positionId идентификатор должности
     * @return допустимые типы транспорта
     */
    Set<String> get(UUID positionId);

    /**
     * Получить все типы транспорта
     * @return типы транспорта
     */
    Set<String> getAll();

}

