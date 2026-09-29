package ru.sber.transport.tariff_fleet.provider;

import ru.sber.transport.tariff_fleet.exception.AwaitingSynchronizationException;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

/**
 * Поставщик данных подразделений.
 */
public interface DepartmentProvider {
    
    /**
     * Удаление подразделения.
     *
     * @param message данные подразделения для удаления.
     */
    void delete(DepartmentMessage message);
    
    /**
     * Сохранение подразделения.
     *
     * @param message данные подразделения для сохранения.
     */
    void save(DepartmentMessage message) throws AwaitingSynchronizationException;
    
}
