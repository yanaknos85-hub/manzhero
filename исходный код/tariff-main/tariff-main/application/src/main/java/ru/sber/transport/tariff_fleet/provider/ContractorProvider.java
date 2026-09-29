package ru.sber.transport.tariff_fleet.provider;


import ru.sber.transport.contractor.messages.ContractorMessage;

public interface ContractorProvider {
    
    /**
     * Удаление контрагента.
     *
     * @param message данные контрагента для удаления. {@link ContractorMessage}
     */
    void delete(ContractorMessage message);
    
    /**
     * Сохранение контрагента.
     *
     * @param message данные контрагента для сохранения. {@link ContractorMessage}
     */
    void save(ContractorMessage message);
}
