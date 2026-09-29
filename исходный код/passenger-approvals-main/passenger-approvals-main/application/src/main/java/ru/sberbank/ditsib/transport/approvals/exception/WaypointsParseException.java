package ru.sberbank.ditsib.transport.approvals.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Waypoints parse exception")
public class WaypointsParseException extends RuntimeException {

    public WaypointsParseException(String message) {
        super(message);
    }
}
