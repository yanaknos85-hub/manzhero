package ru.sber.transport.corporate.business.model;

import java.util.UUID;

/**
 * Модель оргштатной единицы.
 */
public interface HasOrganizationStructure {

    /**
     * Получить идентификатор
     *
     * @return идентификатор
     */
    UUID getId();

    /**
     * Установить идентификатор
     *
     * @param id идентификатор
     */
    void setId(UUID id);

    /**
     * Получить идентификатор организации
     *
     * @return идентификатор организации
     */
    UUID getOrganizationId();

    /**
     * Установить идентификатор организации
     *
     * @param organizationId идентификатор организации
     */
    void setOrganizationId(UUID organizationId);

    /**
     * Получить тип структуры
     *
     * @return тип структуры
     */
    StructureType getStructureType();

    /**
     * Установить тип структуры
     *
     * @param structureType тип структуры
     */
    void setStructureType(StructureType structureType);

    /**
     * Получить идентификатор синхронизации
     *
     * @return идентификатор синхронизации
     */
    String getSyncId();

    /**
     * Установить идентификатор синхронизации
     *
     * @param syncId идентификатор синхронизации
     */
    void setSyncId(String syncId);

    /**
     * Получить человекочитаемый идентификатор
     *
     * @return человекочитаемый идентификатор
     */
    String getHumanReadableId();

    /**
     * Установить человекочитаемый идентификатор
     *
     * @param humanReadableId человекочитаемый идентификатор
     */
    void setHumanReadableId(String humanReadableId);

}
