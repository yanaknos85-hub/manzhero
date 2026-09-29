package ru.sber.transport.authorization.service;

import ru.sber.transport.authorization.model.ConsentCheckModel;

import java.util.function.Function;

/**
 * Функция получения флага подписания ПДн.
 */
public interface ConsentFunction extends Function<ConsentCheckModel, Boolean> {
}
