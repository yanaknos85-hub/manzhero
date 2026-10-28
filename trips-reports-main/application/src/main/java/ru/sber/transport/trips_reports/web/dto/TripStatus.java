package ru.sber.transport.trips_reports.web.dto;

import lombok.Getter;
import lombok.NonNull;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

@Getter
public enum TripStatus {

    /**
     * Опубликовано.
     */
    SENT_TO_CONTRACTOR(false, null, "Отправлена перевозчику"),

    /**
     * Ожидает назначения.
     */
    WAITING_FOR_ASSIGNMENT(false, 1, "Ожидает назначения водителя",TripRequestStatus.TAXI_AWAITING_SEARCH, TripRequestStatus.TAXI_APPROVED, TripRequestStatus.GROUP_TRANSFER_AWAITING_SEARCH, TripRequestStatus.GROUP_TRANSFER_APPROVED),

    /**
     * Водитель найден.
     */
    DRIVER_ASSIGNED(false, 2, "Водитель назначен",TripRequestStatus.TAXI_DRIVER_FOUND, TripRequestStatus.GROUP_TRANSFER_DRIVER_FOUND),

    /**
     * Водитель выехал.
     */
    DRIVER_ON_THE_WAY(false, 4, "Водитель в пути",TripRequestStatus.TAXI_DRIVER_ON_THE_WAY, TripRequestStatus.GROUP_TRANSFER_DRIVER_ON_THE_WAY),

    /**
     * Водитель ожидает клиента.
     */
    DRIVER_ARRIVED(false, 5, "Водитель прибыл в пункт отправления",TripRequestStatus.TAXI_DRIVER_ARRIVED, TripRequestStatus.GROUP_TRANSFER_DRIVER_ARRIVED),

    /**
     * Поездка началась.
     */
    TRIP_IN_PROGRESS(false, 6, "Водитель везет клиента",TripRequestStatus.TAXI_TRIP_IN_PROGRESS, TripRequestStatus.GROUP_TRANSFER_TRIP_IN_PROGRESS),

    /**
     * Остановка в промежуточной точке маршрута.
     */
    INTERMEDIATE_WAYPOINT_ARRIVED(false, null, "Остановка в промежуточной точке маршрута"),

    /**
     * Заказ выполнен.
     */
    ORDER_FINISHED(true, 7, "Поездка завершена",TripRequestStatus.TAXI_TRIP_FINISHED, TripRequestStatus.GROUP_TRANSFER_TRIP_FINISHED),

    /**
     * Клиент отменил заказ.
     */
    ORDER_CANCELLED_BY_CLIENT(true, 8, "Отменена клиентом",TripRequestStatus.TAXI_CANCELLED, TripRequestStatus.GROUP_TRANSFER_CANCELLED),

    /**
     * Водитель отменил заказ.
     */
    ORDER_CANCELLED_BY_DRIVER(true, 9, "Отменена водителем"),

    /**
     * Заказ просрочен.
     */
    ORDER_EXPIRED(true, 10, "Поездка просрочена"),

    /**
     * Неизвестный статус.
     */
    UNDEFINED(false, null, "Не определено");

    private final boolean terminal;

    private final Integer code;

    private final String rusName;
    private final TripRequestStatus[] tripRequestStatuses;

    TripStatus(boolean terminal, Integer code, String rusName,TripRequestStatus... statuses) {
        this.terminal = terminal;
        this.code = code;
        this.rusName = rusName;
        tripRequestStatuses = statuses;
    }

    public static TripStatus getByRusName(@NonNull String rusName){
        for (var tripStatus : TripStatus.values()) {
            if (tripStatus.rusName.equals(rusName)){
                return tripStatus;
            }
        }
        return null;
    }
}
