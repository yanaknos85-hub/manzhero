package ru.sberbank.ditsib.transport.limits.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception throws when parent limit not found.
 */
@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Action not authorized")
public class ActionNotAuthorizedException extends RuntimeException {
    
    /**
     * Create a new exception.
     *
     * @param e exception.
     */
    public ActionNotAuthorizedException(Throwable e) {
        super(e);
    }
    
    /**
     * Create a new exception.
     *
     * @param message message.
     */
    public ActionNotAuthorizedException(String message) {
        super(message);
    }
    
}
