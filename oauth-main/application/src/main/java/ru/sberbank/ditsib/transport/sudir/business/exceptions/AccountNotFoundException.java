package ru.sberbank.ditsib.transport.sudir.business.exceptions;

import java.util.UUID;

/**
 * Исключение, выбрасываемое если аккаунт не найден.
 */
public class AccountNotFoundException extends Exception {
    
    /**
     * Создать исключение.
     *
     * @param login искомый логин.
     */
    public AccountNotFoundException(String login) {
        super(String.format("AccountDto with login '%s' not found", login));
    }
    
    /**
     * Создать исключение.
     *
     * @param userId идентификатор пользователя.
     */
    public AccountNotFoundException(UUID userId) {
        super(String.format("AccountDto with ID '%s' not found", userId));
    }
}
