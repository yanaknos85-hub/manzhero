package ru.sber.transport.corporate_sync;

import org.apache.avro.specific.SpecificRecord;

/**
 * Интерфейс синхронизации
 *
 * @param <T> тип данных
 */
public interface Sync<T extends SpecificRecord> {

    /**
     * Синхронизация данных
     *
     * @param data   данные
     */
    void sync(String organizationId, String id, T data);

}
