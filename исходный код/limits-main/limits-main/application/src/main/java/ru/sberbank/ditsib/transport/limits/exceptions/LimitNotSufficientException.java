package ru.sberbank.ditsib.transport.limits.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;

/**
 * Exception of limit not sufficient.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Limit not sufficient")
public class LimitNotSufficientException extends RuntimeException {
    
    private static final String MSG0 = "Лимит не достаточен. Лимит id %s";
    private static final String MSG1 = "Лимит не достаточен. Лимит id %s, тип транспорта %s";
    private static final String MSG2 = "Лимит не достаточен. Лимит id %s, тип транспорта %s, период %s";
    
    /**
     * Create a new exception.
     *
     * @param limitId ID of limit.
     */
    public LimitNotSufficientException(String limitId) {
        super(String.format(MSG0, limitId));
    }
    
    /**
     * Create a new exception.
     *
     * @param limitId ID of limit.
     */
    public LimitNotSufficientException(String limitId, TransportTypeEnum transportType) {
        super(String.format(MSG1, limitId, transportType.getRusName()));
    }
    
    /**
     * Create a new exception.
     *
     * @param limitId ID of limit.
     */
    public LimitNotSufficientException(String limitId, TransportTypeEnum transportType, Period period) {
        super(String.format(MSG2, limitId, transportType.getRusName(), period.name()));
    }
    
}
