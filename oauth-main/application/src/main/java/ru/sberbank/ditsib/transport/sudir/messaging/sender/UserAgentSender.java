package ru.sberbank.ditsib.transport.sudir.messaging.sender;

import ru.sberbank.ditsib.transport.sudir.business.model.Account;

public interface UserAgentSender {

    /**
     * Отправить сообщение об авторизации.
     *
     * @param account данные пользователя.
     * @param userAgent инфоомация загловка User Agent.
     * @param clientType тип клиента.
     * @param requestUri uri.
     */
    void send(Account account, String userAgent, String clientType, String requestUri);

}
