package ru.sber.transport.tariff_fleet.exception.excel;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.tariff_fleet.exception.BusinessException;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Excel header validation failed")
public class ExcelHeaderValidationException extends BusinessException {
    public ExcelHeaderValidationException() {
        super("Набор столбцов не соответствует формату");
    }
}
