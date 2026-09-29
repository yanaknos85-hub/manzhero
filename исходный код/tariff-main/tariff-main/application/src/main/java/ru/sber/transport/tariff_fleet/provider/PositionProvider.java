package ru.sber.transport.tariff_fleet.provider;

import ru.sber.transport.tariff_fleet.exception.AwaitingSynchronizationException;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

/**
 * Поставщик данных должностей.
 */
public interface PositionProvider {
    
    /**
     * Удаление должности.
     *
     * @param message данные должности для удаления.
     */
    void delete(PositionMessage message);
    
    /**
     * Сохранение должности.
     *
     * @param message данные должности для сохранения.
     */
    void save(PositionMessage message) throws AwaitingSynchronizationException;
    
}
