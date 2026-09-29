package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Have active ewb")
public class HaveActiveEwbException extends BusinessException {

    public static final String DEACTIVATE_CONTRACT_ERR_MSG = "Нельзя удалить договор. Существуют ЭПЛ на будущие даты в рамках этого договора. Отмените ЭПЛ и повторите попытку";
    public static final String DEACTIVATE_TARIFF_ERR_MSG = "Нельзя удалить тариф. Существуют ЭПЛ на будущие даты в рамках этого договора. Отмените ЭПЛ и повторите попытку";

    public HaveActiveEwbException(String message) {
        super(message);
    }
}
