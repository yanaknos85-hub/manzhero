package ru.sberbank.ditsib.transport.sudir.messaging.sender;

import java.util.concurrent.Future;

/**
 * Отправитель данных в черный лист.
 */
public interface BlackListSender {
    
    /**
     * Отправить токен в черный список.
     *
     * @param token токен.
     */
    void send(String token);
}
