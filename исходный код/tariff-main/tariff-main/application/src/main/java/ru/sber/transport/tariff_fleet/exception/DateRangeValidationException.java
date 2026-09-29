package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ошибка проверки диапозона дат
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "DateRange validation exception")
public class DateRangeValidationException extends BusinessException {
    
    public DateRangeValidationException(String message) {
        super(message);
    }
}