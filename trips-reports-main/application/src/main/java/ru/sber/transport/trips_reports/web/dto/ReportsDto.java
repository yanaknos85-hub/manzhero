package ru.sber.transport.trips_reports.web.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO объект, представляющий отчет.
 */
public record ReportsDto(

        /**
         * id отчета.
         */
        UUID id,

        /**
         * Читаемый идентификатор отчета.
         */
        String humanReadableId,

        /**
         * Читаемый идентификатор запроса, связанного с отчетом.
         */
        String requestHumanReadableId,

        /**
         * Желанная дата отчета.
         */
        String desiredDate,

        /**
         * Список стартовых точек маршрута.
         */
        List<String> routeStart,

        /**
         * Список контрольных точек маршрута.
         */
        List<String> routeWaypoints,

        /**
         * Список конечных точек маршрута.
         */
        List<String> routeEnd,

        /**
         * Ожидаемое время ожидания.
         */
        Integer expectedWaitTime,

        /**
         * Ожидаемое расстояние.
         */
        Double expectedDistance,

        /**
         * Минимальная стоимость по расстоянию поездки.
         */
        Double minRideDistanceCost,

        /**
         * Стоимость ожидания за минуту.
         */
        Double waitCostPerMin,

        /**
         * Стоимость поездки за километр.
         */
        Double rideCostPerKm,

        /**
         * Статус отчета.
         */
        String status,

        /**
         * Комментарий к отчету.
         */
        String comment,

        /**
         * Тип поездки.
         */
        String tripType,

        /**
         * Номер контракта, связанного с отчетом.
         */
        String contractNumber,

        /**
         * Полное имя водителя, связанного с отчетом.
         */
        String driverFullName,

        /**
         * Номер транспортного средства, связанного с отчетом.
         */
        String vehicleNumber,

        /**
         * Список полных имен пассажиров.
         */
        List<String> passengerFullNames,

        /**
         * Полное имя автора отчета.
         */
        String authorFullName,

        /**
         * Время создания отчета.
         */
        String creationTime,

        /**
         * Плановая длительность поездки (мин).
         */
        Long expectedTime,

        /**
         * Фактическое расстояние.
         */
        Double factDistance,

        /**
         * Фактическая длительность поездки (мин).
         */
        Long factTime,

        /**
         * Фактичское время ожидания (мин).
         */
        Long factWaitTime,

        /**
         * Фатическая стоимость
         */
        Double factCost
) {}