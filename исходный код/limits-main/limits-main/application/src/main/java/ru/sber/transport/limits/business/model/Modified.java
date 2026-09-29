package ru.sber.transport.limits.business.model;

/**
 * Интерфейс для изменяемых объектов.
 *
 * @param <T> тип объекта.
 */
public interface Modified<T> {

    /**
     * Признак того, что объект был изменен.
     * @return true, если объект был изменен, иначе false.
     */
    boolean modified();

    /**
     * Возвращает объект.
     * @return объект.
     */
    T data();
}
