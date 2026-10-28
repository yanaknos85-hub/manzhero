package ru.sberbank.ditsib.transport.sudir.business.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Данные токенов вошедшего пользователя.
 */
@Getter
@Builder
public class Token {
    
    /**
     * Токен доступа.
     */
    private final String accessToken;
    
    /**
     * Токен обновления сессии.
     */
    private final String refreshToken;
    
}
