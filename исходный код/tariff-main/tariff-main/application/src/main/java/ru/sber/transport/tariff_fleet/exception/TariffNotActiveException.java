
package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY, reason = "Tariff is not active")
public class TariffNotActiveException extends BusinessException {
    public static final String DEACTIVATE_TARIFF_ERROR_MESSAGE = "Тариф уже не активен %s";
    public static final String GET_TARIFF_ERROR_MESSAGE = "Тариф не активен %s";

    public TariffNotActiveException(String template, UUID tariffId) {
        super(template.formatted(tariffId));
    }
}
