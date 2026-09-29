package ru.sberbank.ditsib.transport.limits.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Bonus logic problem")
public class BonusLogicException extends RuntimeException {
    
    public BonusLogicException(String message) {
        super(message);
    }
    
}
