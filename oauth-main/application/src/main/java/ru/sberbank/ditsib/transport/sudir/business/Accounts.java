package ru.sberbank.ditsib.transport.sudir.business;

import ru.sberbank.ditsib.transport.sudir.business.model.Account;
import ru.sberbank.ditsib.transport.sudir.business.model.Role;
import ru.sberbank.ditsib.transport.sudir.business.model.Token;
import ru.sberbank.ditsib.transport.sudir.business.exceptions.AccountNotFoundException;
import ru.sberbank.ditsib.transport.sudir.business.exceptions.RefreshTokenExpired;

import java.security.spec.InvalidKeySpecException;
import java.util.Set;
import java.util.UUID;

/**
 * Пользовательские случаи использования УЗ.
 */
public interface Accounts {
    
    /**
     * Выход из системы.
     *
     * @param userId идентификатор пользователя.
     * @param token токен доступа, для которого производится выход.
     */
    void logout(UUID userId, String token);
    
    /**
     * Продолжение сессии.
     *
     * @param refresh токен обновления сессии.
     * @param userAgent данные заголовка user-agent.
     * @param clientType тип клиента.
     * @return токен доступа.
     * @throws RefreshTokenExpired токен обновления сессии истёк.
     * @throws InvalidKeySpecException неверная спека ключа.
     */
    Token relogin(String refresh, String userAgent, String clientType) throws RefreshTokenExpired, InvalidKeySpecException;

    /**
     * Сохранить данные пользователя.
     *
     * @param account УЗ пользователя.
     */
    void save(Account account);

    /**
     * Вход в систему.
     *
     * @param account УЗ для входа.
     * @return токен.
     * @throws AccountNotFoundException УЗ не найдена.
     * @throws InvalidKeySpecException неверная спека ключа.
     */
    Token login(Account account) throws AccountNotFoundException, InvalidKeySpecException;
    
    /**
     * Получение списка ролей пользователя.
     *
     * @param userId идентификатор пользователя.
     * @return список ролей.
     * @throws AccountNotFoundException УЗ не найдена.
     */
    Set<Role> getRoles(UUID userId) throws AccountNotFoundException;

    /**
     * Активация пользователя.
     *
     * @param id идентификатор пользователя.
     * @param personnelNumber табельный номер пользователя.
     * @param roles роли.
     * @param active флаг активности.
     */
    void activation(UUID id, String personnelNumber, Set<String> roles, boolean active);
}
