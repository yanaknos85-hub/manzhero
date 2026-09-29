package ru.sber.transport.tariff_fleet.service;

import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.model.Contract;

import java.util.Optional;
import java.util.UUID;

/**
 * Вспомогательный сервис для получения данных по контракту без логики
 */
public interface ContractGetInternalService {
    DocumentType getContractType(UUID contractId);

    Optional<Contract> getContract(UUID contractId);
}
