package ru.sberbank.ditsib.transport.limits.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception throws when limit logic is broken.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Limit logic problem number")
public class LimitLogicException extends RuntimeException {
    
    /**
     * Create a new exception.
     *
     * @param message message.
     */
    public LimitLogicException(String message) {
        super(message);
    }
    
    /**
     * Create a new exception.
     *
     * @param e exception.
     */
    public LimitLogicException(Throwable e) {
        super(e);
    }
}
