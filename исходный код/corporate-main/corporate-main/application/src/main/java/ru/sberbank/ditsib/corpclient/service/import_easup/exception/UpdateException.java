package ru.sberbank.ditsib.corpclient.service.import_easup.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Ошибка обновления.
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UpdateException extends Exception {

    /**
     * Класс обрабатываемого сообщения.
     */
    private final Class<?> messageClass;

    /**
     * Значение идентификатора обрабатываемого сообщения
     */
    private final String messageIdentifier;

    /**
     * Класс связанной сущности.
     */
    private final Class<?> entityClass;

    /**
     * Поле идентификатора.
     */
    private final String identifierField;

    /**
     * Значение идентификатора.
     */
    private final String identifier;
}
