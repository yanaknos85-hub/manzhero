package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "department is not active")
public class DepartmentNotActiveException extends BusinessException {

    public DepartmentNotActiveException(UUID id) {
        super(String.format("Не активно подразделение %s", id));
    }
}