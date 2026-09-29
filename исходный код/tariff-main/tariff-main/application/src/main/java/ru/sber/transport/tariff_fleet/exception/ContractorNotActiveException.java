package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Contractor not active")
public class ContractorNotActiveException extends BusinessException {
    
    public ContractorNotActiveException(UUID id) {
        super(String.format("Контрагент %s не действующий", id));
    }
}
