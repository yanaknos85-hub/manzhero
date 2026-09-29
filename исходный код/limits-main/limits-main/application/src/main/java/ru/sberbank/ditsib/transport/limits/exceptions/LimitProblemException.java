package ru.sberbank.ditsib.transport.limits.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception throws when problem found with limit
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Limit problem found")
public class LimitProblemException extends RuntimeException {
    
    /**
     * Create a new exception.
     *
     * @param message message.
     */
    public LimitProblemException(String message) {
        super(message);
    }
    
    /**
     * Create a new exception.
     *
     * @param e exception.
     */
    public LimitProblemException(Throwable e) {
        super(e);
    }
}
