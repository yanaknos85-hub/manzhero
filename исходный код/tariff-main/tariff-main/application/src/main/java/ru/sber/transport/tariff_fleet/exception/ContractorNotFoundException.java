package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Contractor not found")
public class ContractorNotFoundException extends BusinessException {
    
    public ContractorNotFoundException(UUID id) {
        super(String.format("Не найден контрагент ИД: %s", id));
    }
}
