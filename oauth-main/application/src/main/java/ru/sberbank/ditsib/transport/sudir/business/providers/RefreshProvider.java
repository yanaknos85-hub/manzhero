package ru.sberbank.ditsib.transport.sudir.business.providers;

import ru.sberbank.ditsib.transport.sudir.business.model.Account;

import java.util.Map;
import java.util.Optional;

/**
 * Поставщик токенов обновления.
 */
public interface RefreshProvider {
    
    /**
     * Сгенерировать токен.
     *
     * @param accessToken токен доступа.
     * @param account УЗ для генерации токена.
     * @return токен.
     */
    String generate(String accessToken, Account account);
    
    /**
     * Освободить токен обновления.
     *
     * @param account УЗ для поиска токенов.
     * @param token токен доступа.
     */
    void release(Account account, String token);
    
    /**
     * Поиск УЗ с указанным токеном обновления.
     *
     * @param refresh токен обновления.
     * @return УЗ.
     */
    Optional<Account> search(String refresh);
    
    /**
     * Освободить токен обновления.
     *
     * @param refresh токен обновления.
     */
    void release(String refresh);
    
    /**
     * Поиск данных токена.
     *
     * @param login УЗ для поиска данных.
     * @return карта токенов. Ключ - токен доступа, значение - токен обновления сессии.
     */
    Map<String, String> searchTokenData(Account login);
    
    /**
     * Поиск старого токена доступа.
     *
     * @param refresh токен обновления.
     * @return токен доступа.
     */
    String getAccessToken(String refresh);
}
