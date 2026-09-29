package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Service points absent")
public class ServicePointsAbsentException extends BusinessException {
    
    public ServicePointsAbsentException() {
        super("Договор не создан. Для выбранного контрагента добавление сервисных точек обязательно");
    }
}
