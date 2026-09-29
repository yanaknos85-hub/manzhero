package ru.sber.transport.authorization.service;

import org.springframework.lang.NonNull;
import ru.sber.transport.authorization.exceptions.UnauthorizedException;

import java.util.UUID;

/**
 * Сервис проверки доступа пользователя.
 */
public interface CheckUserAccessService {
    
    /**
     * Проверить разрешения пользователя.
     *
     * @throws UnauthorizedException Неавторизованный доступ.
     */
    void check() throws UnauthorizedException;
    
    /**
     * Проверить разрешения пользователя относительно организации.
     *
     * @param organizationId идентификатор организации.
     * @throws UnauthorizedException Неавторизованный доступ.
     */
    void check(@NonNull @lombok.NonNull UUID organizationId) throws UnauthorizedException;
    
}
