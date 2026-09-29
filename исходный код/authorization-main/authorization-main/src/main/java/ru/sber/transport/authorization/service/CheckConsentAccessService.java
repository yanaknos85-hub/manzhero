package ru.sber.transport.authorization.service;

import ru.sber.transport.authorization.exceptions.UnauthorizedException;

/**
 * Сервис проверки доступа внешнего пользователя по факту подписания ПДн.
 */
public interface CheckConsentAccessService {

    /**
     * Проверка доступа внешнего пользователя по факту подписания ПДн.
     *
     * @return Флаг есть ли доступ.
     */
    boolean check() throws UnauthorizedException;
}
