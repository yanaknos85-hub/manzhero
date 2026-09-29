package ru.sber.transport.fraud.monitoring.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Базовая заявка на поездку
 */
public interface TripRequestDataBase {

    /**
     * Идентификатор заявки на поездку
     *
     * @return идентификатор заявки на поездку
     */
    UUID getId();

    /**
     * Вид транспорта
     *
     * @return вид транспорта
     */
    String getTransportType();

    /**
     * Человеко-читаемый идентификатор
     *
     * @return человеко-читаемый идентификатор
     */
    String getHumanReadableId();

    /**
     * Статус заявки на поездку
     *
     * @return статус заявки на поездку
     */
    String getRequestStatus();

    /**
     * Пассажир, который создал заявку на поездку
     *
     * @return пассажир, который создал заявку на поездку
     */
    Employee getPassenger();

    /**
     * Руководитель, который одобрил заявку на поездку
     *
     * @return руководитель, который одобрил заявку на поездку
     */
    Employee getApprover();

    /**
     * Дата поездки
     *
     * @return дата поездки
     */
    OffsetDateTime getDesiredDate();

    /**
     * Дата согласования
     *
     * @return дата согласования
     */
    OffsetDateTime getApprovalDate();

    /**
     * Планируемая стоимость поездки
     *
     * @return стоимость поездки
     */
    BigDecimal getPlannedCost();

    /**
     * Фактическая стоимость поездки
     *
     * @return стоимость поездки
     */
    BigDecimal getActualCost();

    /**
     * МВЗ
     *
     * @return МВЗ
     */
    String getCostCenter();

    /**
     * Цель поездки
     *
     * @return цель поездки
     */
    TripPurpose getPurpose();

    /**
     * Департамент
     *
     * @return департамент
     */
    Department getDepartment();

    /**
     * Путевые точки поездки
     *
     * @return путевые точки поездки
     */
    List<Waypoint> getWaypoints();

    /**
     * Расстояние поездки в километрах
     *
     * @return расстояние поездки
     */
    Double getDistance();

    /**
     * Адрес отправления
     *
     * @return адрес отправления
     */
    String getDepartureAddress();

    /**
     * Адрес назначения
     *
     * @return адрес назначения
     */
    String getDestinationAddress();

    /**
     * Тип компенсации
     *
     * @return тип компенсации
     */
    String getCompensationType();

    /**
     * Время поездки (в секундах)
     *
     * @return время поездки (в секундах)
     */
    Long getDuration();
}