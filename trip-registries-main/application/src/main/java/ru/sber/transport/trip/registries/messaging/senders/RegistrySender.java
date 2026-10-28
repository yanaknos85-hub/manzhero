package ru.sber.transport.trip.registries.messaging.senders;


import ru.sber.transport.database.trip_registries.tables.records.RegistryRecord;

/**
 * Отправитель дополнительных данных для заявок по такси.
 */
public interface RegistrySender {
    
    /**
     * Отправить регистр.
     *
     * @param registry данные для отправки.
     */
    void send(RegistryRecord registry);
}
