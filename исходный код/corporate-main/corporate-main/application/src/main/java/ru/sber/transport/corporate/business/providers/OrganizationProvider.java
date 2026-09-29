package ru.sber.transport.corporate.business.providers;

import lombok.NonNull;
import ru.sber.transport.corporate.business.model.Organization;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер организаций
 */
public interface OrganizationProvider {

    /**
     * Проверка наличия организации по имени
     *
     * @param name имя организации
     * @return признак существования.
     */
    boolean exists(String name);

    /**
     * Сохранение организации
     *
     * @param source организация
     * @return сохраненная организация
     */
    Organization save(Organization source);

    /**
     * Получить список организаций.
     *
     * @return список организаций.
     */
    List<Organization> get();

    /**
     * Получить организацию по идентификатору.
     *
     * @param id идентификатор.
     * @return организация.
     */
    Optional<Organization> get(UUID id);

    /**
     * Проверка наличия организации по идентификатору синхронизации.
     *
     * @param syncId идентификатор синхронизации.
     * @return признак существования.
     */
    boolean existsSync(String syncId);

    /**
     * Получение организации по идентификатору синхронизации.
     *
     * @param organizationId идентификатор организации.
     * @return организация.
     */
    Optional<Organization> get(@NonNull String organizationId);
}
