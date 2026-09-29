package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Nothing to edit")
public class NothingToEditException extends BusinessException {
    
    public NothingToEditException() {
        super("Не переданы данные для редактирования");
    }
}
