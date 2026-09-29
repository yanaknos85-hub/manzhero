package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Edf operator not active")
public class EdfOperatorNotActiveException extends BusinessException {

    public EdfOperatorNotActiveException(String id) {
        super(String.format("Не активен оператор ЭДО %s", id));
    }
}
