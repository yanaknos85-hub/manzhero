package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Ewb tariff already exists in database")
public class EwbTariffAlreadyExistsException extends BusinessException {
    
    public EwbTariffAlreadyExistsException(UUID contractId) {
        super(String.format("У подразделения владельца автопарка уже имеется тариф по договору %s", contractId));
    }
}