package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.PAYLOAD_TOO_LARGE, reason = "Too many service points")
public class TooManyServicePointsException extends BusinessException {
    public TooManyServicePointsException(Integer maxRowsCount) {
        super(String.format("Превышено максимальное количество точек обслуживания. Максимум %s точек", maxRowsCount));
    }
}
