package ru.sber.transport.tariff_fleet.service.grpc;

import java.util.UUID;

/**
 * Сервис для взаимодействия с fuel по grpc
 */
public interface FuelGrpcService {
    /**
     * Деактивация топливных карт, связанных с контрактом
     *
     * @param contractId идентификатор контракта
     */
    void deactivateFuelCardByContractId(UUID contractId);

    /**
     * Деактивация топливных карт, связанных с контрактом и подразделением
     *
     * @param contractId идентификатор контракта
     * @param departmentId идентификатор подразделения
     */
    void deactivateFuelCardByContractAndDepartmentId(UUID contractId, UUID departmentId);
}
