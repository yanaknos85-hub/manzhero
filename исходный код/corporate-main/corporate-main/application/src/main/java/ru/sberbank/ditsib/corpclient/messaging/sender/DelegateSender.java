package ru.sberbank.ditsib.corpclient.messaging.sender;

import ru.sberbank.ditsib.corpclient.database.model.DelegateRecord;

/**
 * Отправитель информации о делегатах.
 */
public interface DelegateSender {

    /**
     * Отправить данные делегата.
     *
     * @param delegateRecord делегат.
     */
    void send(DelegateRecord delegateRecord);

    /**
     * Отправить удаление делегата.
     *
     * @param delegateRecord делегат.
     */
    void sendDelete(DelegateRecord delegateRecord);
}
