package ru.sber.transport.tariff_fleet.service;

public interface ContractAutoActivationService {
    
    /**
     * Активация контракта и связанных с ним тарифов
     */
    void activate();
    
    /**
     * Деактивация контракта и связанных с ним тарифов
     */
    void deactivate();
}
