package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ошибка деактивации топливной карты по тарифу
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Не удалось деактивировать топливные карты по тарифу. Тариф не деактивирован")
public class DeactivateFuelCardTariffException extends RuntimeException {
    public DeactivateFuelCardTariffException() {
        super("Не удалось деактивировать топливные карты по тарифу. Тариф не деактивирован");
    }
}
