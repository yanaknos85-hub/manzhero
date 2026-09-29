package ru.sber.transport.limits.business.exceptions;

import lombok.Getter;
import ru.sber.transport.limits.business.model.Status;

/**
 * Исключение. выбрасываемое при невозможности обновления лимита в данном статусе
 */
@Getter
public class UpdateNotAllowedException extends RuntimeException {

    private final Status status;

    private final String[] fields;

    /**
     * Создать исключение
     *
     * @param status статус лимита
     * @param fields изменяемые поля
     */
    public UpdateNotAllowedException(Status status, String... fields) {
        super("Update not allowed for status: %s for fields: %s".formatted(status, String.join(", ", fields)));
        this.status = status;
        this.fields = fields;
    }
}
