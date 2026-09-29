package ru.sber.transport.integrations.service;

/**
 * Процессор планирования поездок.
 */
public interface CommonApiProcessor {
    
    /**
     * Планирование заявок на поездки.
     */
    void processOutboundCommonApi();
}
