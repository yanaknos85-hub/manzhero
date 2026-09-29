package ru.sber.transport.limits.business.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Исключение, выбрасываемое при отсутствии типа в лимитах.
 */
@Getter
@RequiredArgsConstructor
public class TypeNotAvailableException extends Exception {

    /**
     * Идентификатор типа.
     */
    private final String name;

}
