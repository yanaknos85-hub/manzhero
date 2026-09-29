package ru.sber.transport.fraud.monitoring.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Интерфейс заявки на поездку
 */
public interface TripRequest {

    /**
     * Идентификатор заявки
     *
     * @return идентификатор заявки
     */
    UUID getId();

    /**
     * Человекочитаемый идентификатор заявки
     *
     * @return идентификатор заявки
     */
    String getHumanReadableId();

    /**
     * Тип транспорта
     *
     * @return тип транспорта
     */
    TransportType getTransportType();

    /**
     * Тариф
     *
     * @return тариф
     */
    String getTariff();

    /**
     * Идентификатор пассажира
     *
     * @return идентификатор пассажира
     */
    UUID getPassengerId();

    /**
     * Идентификатор согласованта
     *
     * @return идентификатор согласованта
     */
    UUID getApproverId();

    /**
     * Желаемая дата и время поездки
     *
     * @return дата поездки
     */
    OffsetDateTime getDesiredDate();

    /**
     * Дата согласования заявки
     *
     * @return дата согласования заявки
     */
    OffsetDateTime getApprovalDate();

    /**
     * Планируемая стоимость поездки
     *
     * @return планируемая стоимость поездки
     */
    BigDecimal getPlannedCost();

    /**
     * Фактическая стоимость поездки
     *
     * @return фактическая стоимость поездки
     */
    BigDecimal getActualCost();

    /**
     * Идентификатор организации
     *
     * @return идентификатор организации
     */
    UUID getOrganizationId();

    /**
     * Идентификатор департамента
     *
     * @return идентификатор департамента
     */
    UUID getDepartmentId();

    /**
     * Статус заявки
     *
     * @return статус заявки
     */
    String getRequestStatus();

    /**
     * Идентификатор цели поездки
     *
     * @return идентификатор цели поездки
     */
    UUID getPurposeId();

    /**
     * Тайм зона
     *
     * @return тайм зона
     */
    String getTimeZone();

    /**
     * Список путевых точек
     *
     * @return список путевых точек
     */
    List<Waypoint> getWaypoints();

    /**
     * Расстояние поездки в километрах
     *
     * @return расстояние поездки
     */
    Double getDistance();

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
