package ru.sber.transport.fraud.monitoring.model;

/**
 * Данные о сортировке.
 */
public interface SortData {

    /**
     * Возвращает поле для сортировки.
     *
     * @return поле для сортировки
     */
    String field();

    /**
     * Возвращает направление сортировки.
     *
     * @return направление сортировки
     */
    boolean asc();

}
