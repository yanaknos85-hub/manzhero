package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Service points not unique")
public class ServicePointsUniqueException extends BusinessException {
    
    public ServicePointsUniqueException() {
        super("Договор не создан. Не должно быть Автосервисов с одинаковыми адресами и координатами");
    }
}
