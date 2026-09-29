package ru.sber.transport.fraud.monitoring.providers;


import ru.sber.transport.fraud.monitoring.model.Organization;

import java.util.UUID;

/**
 * Провайдер работы с организациями (gRPC)
 */
public interface OrganizationsGrpcProvider {


    /**
     * Получает организацию по идентификатору
     *
     * @param id идентификатор организации
     * @return организация
     */
    Organization get(UUID id);
}
