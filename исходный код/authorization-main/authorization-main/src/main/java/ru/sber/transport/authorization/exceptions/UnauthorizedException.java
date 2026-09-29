package ru.sber.transport.authorization.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Исключение, выпадающее при неавторизованом доступе.
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Unauthorized")
public class UnauthorizedException extends RuntimeException {

    /**
     * Создание исключения.
     *
     * @param employeeId идентификатор сотрудника.
     */
    public UnauthorizedException(UUID employeeId) {
        super(String.format("Trying to unauthorized access!!! Employee: %s", employeeId));
    }

    /**
     * Создание исключения.
     *
     * @param reason причина исключения.
     */
    public UnauthorizedException(String reason) {
        super(reason);
    }
}
