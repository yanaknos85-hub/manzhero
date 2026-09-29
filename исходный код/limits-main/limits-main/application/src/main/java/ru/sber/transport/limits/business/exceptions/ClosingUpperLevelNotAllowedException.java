package ru.sber.transport.limits.business.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Ошибка закрытия верхнего уровня
 */
@Getter
@RequiredArgsConstructor
public class ClosingUpperLevelNotAllowedException extends RuntimeException {

    private final UUID limitId;

}
