package ru.sber.transport.fraud.monitoring.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Фильтр заявок на поездку
 */
public interface RequestFilter {

    /**
     * Имя согласующего
     *
     * @return имя согласующего
     */
    String approverName();

    /**
     * Имя пассажира
     *
     * @return имя пассажира
     */
    String passengerName();

    /**
     * Список идентификаторов пассажиров
     *
     * @return список идентификаторов пассажиров
     */
    List<UUID> passenger();

    /**
     * Список идентификаторов согласующих
     *
     * @return список идентификаторов согласующих
     */
    List<UUID> approver();

    /**
     * Список целей поездки
     *
     * @return список идентификаторов согласующих
     */
    List<UUID> purpose();

    /**
     * Список типов транспорта
     *
     * @return список типов транспорта
     */
    List<String> transportType();

    /**
     * Дата начала заявки - от
     *
     * @return дата начала заявки - от
     */
    OffsetDateTime tripDateStart();

    /**
     * Дата начала заявки - до
     *
     * @return дата начала заявки - до
     */
    OffsetDateTime tripDateEnd();

    /**
     * Дата согласования заявки - от
     *
     * @return дата согласования заявки - от
     */
    OffsetDateTime approveDateStart();

    /**
     * Дата согласования заявки - до
     *
     * @return дата согласования заявки - до
     */
    OffsetDateTime approveDateEnd();


    /**
     * Фильтр заявки по ее человеко-читаемому идентификатору
     *
     * @return идентификатор заявки
     */
    String humanReadableId();

}
