package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "File has no name")
public class FileNoNameException extends BusinessException {
    public FileNoNameException() {
        super("Не указано имя файла");
    }
}
