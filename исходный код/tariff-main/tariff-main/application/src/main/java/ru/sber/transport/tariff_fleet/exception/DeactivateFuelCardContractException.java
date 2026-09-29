package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ошибка деактивации топливной карты по договору
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Не удалось деактивировать топливные карты по договору. Договор не деактивирован")
public class DeactivateFuelCardContractException extends BusinessException {
    public DeactivateFuelCardContractException() {
        super("Не удалось деактивировать топливные карты по договору. Договор не деактивирован");
    }
}
