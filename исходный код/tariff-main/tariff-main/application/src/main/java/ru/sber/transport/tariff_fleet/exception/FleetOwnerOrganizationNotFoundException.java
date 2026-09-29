package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Fleet owner organization not found")
public class FleetOwnerOrganizationNotFoundException extends BusinessException {

    public FleetOwnerOrganizationNotFoundException(UUID id) {
        super(String.format("Не найдена организация владельца автопарка %s", id));
    }
}