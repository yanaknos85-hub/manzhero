package ru.sberbank.ditsib.transport.limits.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception throws when trying to reserve more then remains.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Trying to reserve more then remains")
public class ReservationFailedException extends RuntimeException {
    
    /**
     * Create a new exception.
     *
     * @param message message.
     */
    public ReservationFailedException(String message) {
        super(message);
    }
}
