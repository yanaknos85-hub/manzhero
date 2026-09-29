package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Fleet owner department not found")
public class FleetOwnerDepartmentNotFoundException extends BusinessException {
    
    public FleetOwnerDepartmentNotFoundException(UUID id) {
        super(String.format("Не найдено подразделение владельца автопарка %s", id));
    }
}