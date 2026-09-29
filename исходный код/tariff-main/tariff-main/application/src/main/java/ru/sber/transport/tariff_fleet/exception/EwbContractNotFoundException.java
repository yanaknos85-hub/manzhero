package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Ewb contract not found")
public class EwbContractNotFoundException extends BusinessException {
    
    public EwbContractNotFoundException(UUID id) {
        super(String.format("Не найден договор на услугу Выпуск на линию %s", id));
    }
}
