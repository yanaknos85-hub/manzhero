package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Fleet owner department not active")
public class FleetOwnerDepartmentNotActiveException extends BusinessException {
    
    public FleetOwnerDepartmentNotActiveException(UUID id) {
        super(String.format("Не активно подразделение владельца автопарка %s", id));
    }
}