package ru.sberbank.ditsib.corpclient.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;

/**
 * Исключение, выбрасываемое при конфликте данных.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Conflicted request")
@Getter
public class DataConflictException extends RuntimeException {
    
    private final Class<?> entity;
    
    private final ConflictType type;
    
    private final Serializable changed;
    
    private final Serializable conflicted;
    
    private final String conflictedField;
    
    /**
     * Создать новое исключение.
     *
     * @param reason причина конфликта.
     */
    public DataConflictException(String reason) {
        super(reason);
        entity = null;
        type = null;
        changed = null;
        conflicted = null;
        conflictedField = null;
    }
    
    /**
     * Создать новое исключение.
     *
     * @param entity сущность с конфликтующими данными.
     * @param type тип конфликта.
     * @param changed измененная сущность.
     * @param conflictedField конфликтное поле.
     * @param conflicted конфликтное значение.
     */
    public DataConflictException(Class<?> entity, ConflictType type, Serializable changed,
                                 String conflictedField, Serializable conflicted) {
        this.entity = entity;
        this.conflictedField = conflictedField;
        this.type = type;
        this.changed = changed;
        this.conflicted = conflicted;
    }
    
    /**
     * Типы конфликтов.
     */
    public enum ConflictType {
        INFINITE_LOOP,
        ORGANIZATION_ALREADY_IN_GROUP,
        ORGANIZATION_GROUP_WITH_CURRENT_NAME_ALREADY_EXISTS
    }
}
