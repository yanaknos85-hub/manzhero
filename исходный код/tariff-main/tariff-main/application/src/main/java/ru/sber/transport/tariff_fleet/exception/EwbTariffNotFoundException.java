package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Ewb tariff not found")
public class EwbTariffNotFoundException extends BusinessException {
    public EwbTariffNotFoundException(UUID tariffId) {
        super(String.format("Не найден тариф на услугу Выпуск на линию id:%s", tariffId));
    }
}
