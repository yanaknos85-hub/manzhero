package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.database.model.messages.Tariff;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с тарифами каршеринга
 */
public interface TariffService {
    
    /**
     * Создать / отредактировать тариф
     * @param tariff CarsharingTariff
     * @return сохраненный CarsharingTariff
     */
    Tariff save(Tariff tariff);
    
    /**
     * Найти тариф по его ID
     * @param tariffId ID тарифа
     * @return тариф
     */
    Optional<Tariff> getOptionalById(UUID tariffId);
    
    /**
     * Найти тариф по его ID
     * @param tariffId ID тарифа
     * @return тариф каршеринга
     */
    Tariff getTariffById(UUID tariffId);

    /**
     * Удалить тариф каршеринга, заранее найденный в БД
     * @param tariff тариф
     */
    void delete(Tariff tariff);
}
