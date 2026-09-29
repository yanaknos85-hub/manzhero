package ru.sberbank.ditsib.transport.approvals.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid sort property")
public class InvalidSortPropertyException extends RuntimeException {

    public InvalidSortPropertyException() {
        super("Указаны недопустимые свойства сортировок");
    }
}
