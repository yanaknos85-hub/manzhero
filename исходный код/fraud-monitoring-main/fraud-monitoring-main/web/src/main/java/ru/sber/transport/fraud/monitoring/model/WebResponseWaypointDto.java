package ru.sber.transport.fraud.monitoring.model;


import ru.sber.transport.web.model.WaypointDto;

/**
 * Объект ответа с путевыми точками
 */
public class WebResponseWaypointDto extends WaypointDto {

    public WebResponseWaypointDto(Waypoint waypoint) {
        setAddress(waypoint.getAddress());
        setWaitTime(waypoint.getWaitTime());
    }
}
