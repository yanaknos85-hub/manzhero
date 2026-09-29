package ru.sberbank.ditsib.transport.limits.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Employee not found")
public class EmployeeNotFoundException extends RuntimeException {
    
    public EmployeeNotFoundException(UUID ownerUUID) {
        super("Employee %s not found".formatted(ownerUUID));
    }
    
}
