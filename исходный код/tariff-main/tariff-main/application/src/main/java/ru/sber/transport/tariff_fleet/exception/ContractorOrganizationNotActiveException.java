package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Contractor organization not active")
public class ContractorOrganizationNotActiveException extends BusinessException {
    
    public ContractorOrganizationNotActiveException(UUID id) {
        super(String.format("Не активна организация контрагента %s", id));
    }
}
