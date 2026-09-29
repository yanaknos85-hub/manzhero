
package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY, reason = "Tariff already existed")
public class TariffAlreadyExistedException extends BusinessException {

    public TariffAlreadyExistedException(UUID tariffId, UUID contractId) {
        super("Уже существует тариф %s по договору %s".formatted(tariffId, contractId));
    }

    public TariffAlreadyExistedException(UUID tariffId, UUID contractId, UUID departamentId) {
        super("У подразделения %s же существует тариф %s по договору  %s".formatted(departamentId, tariffId, contractId));
    }
}
