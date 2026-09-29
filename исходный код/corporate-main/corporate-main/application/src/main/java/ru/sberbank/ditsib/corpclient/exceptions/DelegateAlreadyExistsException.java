package ru.sberbank.ditsib.corpclient.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Exception of delegate record already added.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Delegate already added")
public class DelegateAlreadyExistsException extends RuntimeException {
    
    public static final String UNIQUE_MSG_FORMAT =
            "Delegate for supervisor with id = %s with  delegateId = %s already have " +
            "record starting at %s";
    
    public static final String DATE_RANGE_MSG_FORMAT = "Employee with id = %s already have records for supervisor " +
                                                       "with id = %s crossing specified date range(from = %s; to = %s)";
    
    /**
     * Create a new exception.
     *
     * @param supervisorId supervisor id
     * @param delegateId delegate id
     * @param from date from
     */
    public DelegateAlreadyExistsException(UUID supervisorId, UUID delegateId, LocalDate from) {
        super(String.format(UNIQUE_MSG_FORMAT, supervisorId, delegateId, from));
    }
    
    /**
     * Create a new exception.
     *
     * @param supervisorId supervisor id
     * @param delegateId delegate id
     */
    public DelegateAlreadyExistsException(UUID supervisorId, UUID delegateId, LocalDate from, LocalDate to) {
        super(String.format(DATE_RANGE_MSG_FORMAT, supervisorId, delegateId, from, to));
    }
}
