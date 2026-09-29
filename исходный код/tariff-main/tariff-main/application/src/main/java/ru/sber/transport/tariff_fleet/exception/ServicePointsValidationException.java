package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Service points validation error")
public class ServicePointsValidationException extends BusinessException {

    public static final String INVALID_LATITUDE_VALUE_MSG = "Невалидное значение широты: допускается значение в диапазоне от -90 до 90 градусов";
    public static final String INVALID_LONGITUDE_VALUE_MSG = "Невалидное значение долготы: допускается значение в диапазоне от -180 до 180 градусов";
    public static final String INVALID_LATITUDE_SCALE_VALUE_MSG = "Невалидное значение широты: допускается не более %s цифр после запятой";
    public static final String INVALID_LONGITUDE_SCALE_VALUE_MSG = "Невалидное значение долготы: допускается не более %s цифр после запятой";

    public ServicePointsValidationException(String message) {
        super(message);
    }

    public ServicePointsValidationException(String message, String argument) {
        super(String.format(message, argument));
    }
}
