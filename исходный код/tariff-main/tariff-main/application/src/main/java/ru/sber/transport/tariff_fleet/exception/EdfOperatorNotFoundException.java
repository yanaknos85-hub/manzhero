package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Edf operator not found")
public class EdfOperatorNotFoundException extends BusinessException {

    public EdfOperatorNotFoundException(String id) {
        super(String.format("Не найден оператор ЭДО %s", id));
    }
}
