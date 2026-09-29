package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.database.model.Organization;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с корп.клиентами
 */
public interface OrganizationService {
    
    /**
     * Получить корп. клиента
     * @param id ID корп.клиента
     * @return получить организацию по идентификатору
     */
    Optional<Organization> get(UUID id);
    
    /**
     * Получить корп. клиента или исключение
     * @param id ID корп.клиента
     * @return корп.клиент
     */
    Organization getOrThrow(UUID id);
    
    /**
     * Удалить корп. клиента
     * @param organization корп.клиент
     */
    void delete(Organization organization);
    
    /**
     * Сохранить корп.клиента
     * @param organization корп.клиента
     */
    void save(Organization organization);

    /**
     * Сохраняем организацию, которую мы получим по grpc из сервиса corporate
     *
     * @param message Сообщение в случае ошибки
     * @param id Идентификатор записи об организации
     */
    void saveGrpcEntity(String message, UUID id);
}
