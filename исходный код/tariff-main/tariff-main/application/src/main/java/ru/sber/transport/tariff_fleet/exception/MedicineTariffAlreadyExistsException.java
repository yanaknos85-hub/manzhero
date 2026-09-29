package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Department already has medicine tariff")
public class MedicineTariffAlreadyExistsException extends BusinessException {

    private static final String MSG = "Подразделение id=%s уже имеет тариф на медицинские услуги";

    public MedicineTariffAlreadyExistsException(UUID departmentId) {
        super(MSG.formatted(departmentId));
    }
}
