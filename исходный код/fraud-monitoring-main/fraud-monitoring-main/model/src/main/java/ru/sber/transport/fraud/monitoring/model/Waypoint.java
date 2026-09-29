package ru.sber.transport.fraud.monitoring.model;

import java.util.UUID;

/**
 * Интерфейс путевой точки
 */
public interface Waypoint {

    /**
     * Идентификатор путевой точки
     *
     * @return идентификатор путевой точки
     */
    UUID getId();

    /**
     * Идентификатор заявки на поездку
     *
     * @return идентификатор заявки на поездку
     */
    UUID getTripRequestId();

    /**
     * Страна
     *
     * @return страна
     */
    String getCountry();

    /**
     * Регион
     *
     * @return регион
     */
    String getRegion();

    /**
     * Город
     *
     * @return город
     */
    String getCity();

    /**
     * Улица
     *
     * @return улица
     */
    String getStreet();

    /**
     * Дом
     *
     * @return дом
     */
    String getHouse();

    /**
     * Строение
     *
     * @return строение
     */
    String getStructure();

    /**
     * Корпус
     *
     * @return корпус
     */
    String getBuilding();

    /**
     * Порядковый номер точки
     *
     * @return порядковый номер точки
     */
    Integer getOrderingIndex();

    /**
     * Время ожидания в промежуточной точке
     *
     * @return время ожидания в промежуточной точке
     */
    Long getWaitTime();

    /**
     * Адрес
     *
     * @return адрес
     */
    String getAddress();

}
