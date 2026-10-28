package ru.sber.transport.trips_reports.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Модель, представляющая отчет.
 */
@Getter
@Setter
public class Reports {

    /**
     * id отчета.
     */
    UUID id;

    /**
     * Читаемый идентификатор отчета.
     */
    private String humanReadableId;

    /**
     * Читаемый идентификатор запроса, связанного с отчетом.
     */
    private String requestHumanReadableId;

    /**
     * Желанная дата отчета.
     */
    private String desiredDate;

    /**
     * Список стартовых точек маршрута.
     */
    private List<String> routeStart;

    /**
     * Список контрольных точек маршрута.
     */
    private List<String> routeWaypoints;

    /**
     * Список конечных точек маршрута.
     */
    private List<String> routeEnd;

    /**
     * Ожидаемое время ожидания.
     */
    private Integer expectedWaitTime;

    /**
     * Ожидаемое расстояние.
     */
    private Double expectedDistance;

    /**
     * Минимальная стоимость по расстоянию поездки.
     */
    private Double minRideDistanceCost;

    /**
     * Стоимость ожидания за минуту.
     */
    private Double waitCostPerMin;

    /**
     * Стоимость поездки за километр.
     */
    private Double rideCostPerKm;

    /**
     * Статус отчета.
     */
    private String status;

    /**
     * Комментарий к отчету.
     */
    private String comment;

    /**
     * Тип поездки.
     */
    private String tripType;

    /**
     * Номер контракта, связанного с отчетом.
     */
    private String contractNumber;

    /**
     * Полное имя водителя, связанного с отчетом.
     */
    private String driverFullName;

    /**
     * Номер транспортного средства, связанного с отчетом.
     */
    private String vehicleNumber;

    /**
     * Список полных имен пассажиров.
     */
    private List<String> passengerFullNames;

    /**
     * Полное имя автора отчета.
     */
    private String authorFullName;

    /**
     * Время создания отчета.
     */
    private String creationTime;

    /**
     * Плановая длительность поездки (миллисекунды).
     */
    private Long expectedTime;

    /**
     * Фактическое расстояние.
     */
    private Double factDistance;

    /**
     * Фактическая длительность поездки (мин).
     */
    private Long factTime;

    /**
     * Фактичское время ожидания (мин).
     */
    private Long factWaitTime;

    /**
     * Фатическая стоимость
     */
    private Double factCost;
}
