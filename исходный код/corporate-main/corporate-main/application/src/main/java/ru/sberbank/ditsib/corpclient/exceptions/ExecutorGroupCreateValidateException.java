package ru.sberbank.ditsib.corpclient.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY, reason = "Exceptions with create entity")
public class ExecutorGroupCreateValidateException extends RuntimeException {

    public ExecutorGroupCreateValidateException(String e) {
        super(e);
    }
}
