package ru.sber.transport.trip.registries.service;

/**
 * Процессор реестров.
 */
public interface RegistryProcessor {
    
    /**
     * Планирование заявок на поездки.
     */
    void processRegistries();
}
