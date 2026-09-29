package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Tariff not found")
public class TariffNotFoundException extends BusinessException {
    public TariffNotFoundException(UUID tariffId) {
        super(String.format("Не найден тариф ID:%s", tariffId));
    }
}
