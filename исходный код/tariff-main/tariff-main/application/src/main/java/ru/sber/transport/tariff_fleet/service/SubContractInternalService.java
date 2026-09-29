package ru.sber.transport.tariff_fleet.service;

import ru.sber.transport.tariff_fleet.database.model.AbstractContract;

import java.util.UUID;

/**
 * Вспомогательный параметризованный сервис для получения данных по контракту без логики
 */
public interface SubContractInternalService<E extends AbstractContract> {
    /**
     * Getting a fuel contract
     *
     * @param contractId contract identifier
     * @return contract
     */
    E getContract(UUID contractId);
}
