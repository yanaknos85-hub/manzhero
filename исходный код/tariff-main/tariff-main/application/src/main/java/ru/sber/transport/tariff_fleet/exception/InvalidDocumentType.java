package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid document type")
public class InvalidDocumentType extends BusinessException {
    public InvalidDocumentType(String type) {
        super("Invalid document type: " + type);
    }
}
