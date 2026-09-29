package ru.sber.transport.fraud.monitoring.providers;


import ru.sber.transport.fraud.monitoring.model.Waypoint;

/**
 * Провайдер данных о путевых точках.
 */
public interface WaypointsDatabaseProvider {

    /**
     * Сохраняет данные о путевой точке.
     *
     * @param source данные о путевой точке
     * @return сохранённые данные о путевой точке
     */
    Waypoint save(Waypoint source);

}
