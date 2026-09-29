package ru.sberbank.ditsib.corpclient.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ошибка, выбрасываемая при обновлении внутреннего пользователя.
 */
@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Update internal user forbidden")
public class UpdateForbiddenException extends RuntimeException {

    /**
     * Создать новое исключение.
     */
    public UpdateForbiddenException() {
        super("Update internal user forbidden");
    }

}
