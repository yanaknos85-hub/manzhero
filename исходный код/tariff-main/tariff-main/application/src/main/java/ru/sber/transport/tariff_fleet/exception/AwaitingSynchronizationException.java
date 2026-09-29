package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Synchronization exception")
public class AwaitingSynchronizationException extends BusinessException {

    public AwaitingSynchronizationException(String message) {
        super(message);
    }
}
