package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Ewb contract already exists in database")
public class EwbContractAlreadyExistsException extends BusinessException {
    
    public EwbContractAlreadyExistsException(String number) {
        super(String.format("В системе имеется договор с указанными параметрами. Номер договора: %s", number));
    }
}