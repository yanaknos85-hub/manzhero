package ru.sber.transport.fraud.monitoring.business;



import ru.sber.transport.fraud.monitoring.model.Organization;

import java.util.UUID;

/**
 * Сервис работы с организациями
 */
public interface OrganizationsService {

    /**
     * Сохраняет организацию
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
    Organization getExistedOrCreate(UUID id);
}
