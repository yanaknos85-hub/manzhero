package ru.sber.transport.corporate_sync.cache;

import ru.sber.transport.corporate.business.model.Filter;
import ru.sber.transport.corporate.business.model.HasOrganizationStructure;

import java.util.Optional;
import java.util.UUID;

/**
 * Кэш организаций
 */
public interface OrganizationsCache {

    /**
     * Указать организацию
     *
     * @param organizationId идентификатор организации
     */
    void organization(String organizationId);

    /**
     * Очистка кэша
     */
    void clear();

    /**
     * Получить организацию
     *
     * @return организация
     */
    UUID getOrganization();

    /**
     * Получить данные для синхронизации
     * @param dataClass класс данных
     * @param syncId идентификатор
     * @return идентификатор объекта
     */
    <T extends HasOrganizationStructure, F extends Filter> Optional<T> get(Class<T> dataClass, Class<F> filter, String syncId);

    /**
     * Сохранить данные для синхронизации
     * @param dataClass класс данных
     * @param syncId идентификатор
     * @param element объект
     */
    <T extends HasOrganizationStructure> void put(Class<T> dataClass, String syncId, T element);

    /**
     * Проверка на пустоту
     *
     * @return пристутсвуют ли данные в кэше
     */
    boolean isEmpty();

}
