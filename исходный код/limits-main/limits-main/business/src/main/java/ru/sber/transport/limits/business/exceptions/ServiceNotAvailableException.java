package ru.sber.transport.limits.business.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Исключение, выбрасываемое при отсутствии услуги в лимитах.
 */
@RequiredArgsConstructor
@Getter
public class ServiceNotAvailableException extends Exception {

    /**
     * Идентификатор услуги.
     */
    private final String name;

}
