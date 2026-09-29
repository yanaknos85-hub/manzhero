package ru.sberbank.ditsib.corpclient.messaging.sender;

import java.util.Collection;

/**
 * Отправитель данных.
 *
 * @param <T> тип данных.
 */
public interface Sender<T> {

    /**
     * Отправить.
     *
     * @param toSend коллекция для отправки.
     */
    void send(Collection<T> toSend);

}
