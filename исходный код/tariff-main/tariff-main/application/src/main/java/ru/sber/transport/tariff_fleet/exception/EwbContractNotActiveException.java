package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Ewb contract not active")
public class EwbContractNotActiveException extends BusinessException {
    
    public EwbContractNotActiveException(UUID id) {
        super("Не активен договор на услугу Выпуск на линию %s".formatted(id));
    }
}
