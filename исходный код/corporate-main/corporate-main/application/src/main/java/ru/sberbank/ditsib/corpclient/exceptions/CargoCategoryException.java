package ru.sberbank.ditsib.corpclient.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sberbank.ditsib.transport.constants.cargo.CargoCategoryEnum;

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
public class CargoCategoryException extends RuntimeException {

    private static final String MESSAGE = "Cargo category %s is not allowed";

    public CargoCategoryException(CargoCategoryEnum cargoCategory) {
        super(String.format(MESSAGE, cargoCategory));
    }
}
