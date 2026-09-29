package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Contractor organization not found")
public class ContractorOrganizationNotFoundException extends BusinessException {
    
    public ContractorOrganizationNotFoundException(UUID id) {
        super(String.format("Не найдена организация контрагента %s", id));
    }
}
