package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Ewb tariff not active")
public class EwbTariffNotActiveException extends BusinessException {
    public EwbTariffNotActiveException(UUID tariffId) {
        super("Не активен тариф на услугу Выпуск на линию %s".formatted(tariffId));
    }
}
