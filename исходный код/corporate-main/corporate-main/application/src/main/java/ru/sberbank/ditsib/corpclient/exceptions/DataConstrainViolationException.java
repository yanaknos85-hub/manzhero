package ru.sberbank.ditsib.corpclient.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;
import java.util.Map;

/**
 * Исключение, выбрасываемое при конфликте данных.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Date Constrain violation")
@Getter
public class DataConstrainViolationException extends RuntimeException {

    private final Class<?> entity;

    private final ConflictType type;

    private final Serializable changed;

    private final Map<String, Serializable> conflicted;

    /**
     * Создать новое исключение.
     *
     * @param entity сущность с конфликтующими данными.
     * @param type тип конфликта.
     * @param changed измененная сущность.
     * @param conflicted конфликтное значение.
     */
    public DataConstrainViolationException(Class<?> entity, ConflictType type, Serializable changed,
                                           Map<String, Serializable> conflicted) {
        this.entity = entity;
        this.type = type;
        this.changed = changed;
        this.conflicted = conflicted;
    }
    
    /**
     * Типы конфликтов.
     */
    public enum ConflictType {
        DATE_CONSTRAIN_VIOLATION
    }
}
