package ru.sberbank.ditsib.transport.sudir.messaging.sender;

import ru.sberbank.ditsib.transport.sudir.business.model.Account;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

/**
 * Отправить данные УЗ.
 */
public interface AccountSender {

    /**
     * Отправить данные.
     *
     * @param account УЗ.
     */
    void send(Account account);

    /**
     * Отправить данные на удаление.
     *
     * @param accounts список УЗ.
     */
    void sendDeleted(Collection<UUID> accounts);

    /**
     * Отправить данные о последнем входе.
     *
     * @param account входящий УЗ.
     * @param loggedIn время входа.
     */
    void send(Account account, LocalDateTime loggedIn);
}
