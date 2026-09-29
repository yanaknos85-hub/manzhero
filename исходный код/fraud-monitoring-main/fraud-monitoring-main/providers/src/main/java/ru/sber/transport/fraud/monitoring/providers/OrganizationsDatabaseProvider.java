package ru.sber.transport.fraud.monitoring.providers;


import ru.sber.transport.fraud.monitoring.model.Organization;

import java.util.UUID;

/**
 * Провайдер работы с организациями (БД)
 */
public interface OrganizationsDatabaseProvider {

    /**
     * Сохраняет или обновляет данные об организации
     *
     * @param source организация для сохранения
     * @return сохраненная организация
     */
    Organization createOrUpdate(Organization source);

    /**
     * Получает организацию по идентификатору
     *
     * @param id идентификатор организации
     * @return организация
     */
    Organization get(UUID id);
}
