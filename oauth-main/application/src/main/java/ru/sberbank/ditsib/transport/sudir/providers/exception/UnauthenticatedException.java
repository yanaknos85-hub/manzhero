package ru.sberbank.ditsib.transport.sudir.providers.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Исключение, выпадающее при неавторизованом доступе.
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Unauthorized")
public class UnauthenticatedException extends RuntimeException {

    /**
     * Создание исключения.
     *
     * @param employeeId идентификатор сотрудника.
     */
    public UnauthenticatedException(UUID employeeId) {
        super(String.format("Trying to unauthorized access!!! Employee: %s", employeeId));
    }
    
    /**
     * Создание исключения.
     *
     * @param e корневое исключение.
     */
    public UnauthenticatedException(Throwable e) {
        super(e);
    }
    
    /**
     * Создание исключения.
     *
     * @param reason причина исключения.
     */
    public UnauthenticatedException(String reason) {
        super(reason);
    }
}
