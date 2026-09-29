package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.database.model.messages.TaxiTariff;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с тарифами каршеринга
 */
public interface TaxiTariffService {
    
    /**
     * Создать / отредактировать тариф
     * @param tariff CarsharingTariff
     * @return сохраненный CarsharingTariff
     */
    TaxiTariff save(TaxiTariff tariff);
    
    /**
     * Найти тариф по его ID
     * @param tariffId ID тарифа
     * @return тариф такси
     */
    Optional<TaxiTariff> getOptionalById(UUID tariffId);
    
    /**
     * Найти тариф по его ID
     * @param tariffId ID тарифа
     * @return тариф каршеринга
     */
    TaxiTariff getTariffById(UUID tariffId);

    /**
     * Удалить тариф каршеринга, заранее найденный в БД
     * @param tariff тариф
     */
    void delete(TaxiTariff tariff);
}
