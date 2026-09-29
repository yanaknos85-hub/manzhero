package ru.sberbank.ditsib.transport.approvals.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Synchronization exception")
public class AwaitingSynchronizationException extends RuntimeException {

    public AwaitingSynchronizationException(String message) {
        super(message);
    }
}
