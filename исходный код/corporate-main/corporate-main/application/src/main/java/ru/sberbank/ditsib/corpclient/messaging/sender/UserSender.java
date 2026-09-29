package ru.sberbank.ditsib.corpclient.messaging.sender;

import ru.sberbank.ditsib.corpclient.database.model.Employee;

import java.util.Collection;

/**
 * Отправить данные пользователя.
 */
public interface UserSender {
    
    /**
     * Отправить данные нового пользователя.
     *
     * @param newUser данные пользователя.
     */
    void send(Employee newUser, Collection<String> roles);

    /**
     * Отправить данные нового пользователя.
     *
     * @param source данные пользователя.
     */
    void send(ru.sber.transport.corporate.business.model.Employee source, Collection<String> roles);
}
