package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Wrong contract change event")
public class WrongContractChangeEventException extends RuntimeException {
    public WrongContractChangeEventException(Object event) {
        super("Неподдерживаемый тип события %s".formatted(event.getClass().getName()));
    }
}
