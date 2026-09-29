package ru.sber.transport.corporate.business.senders;

import java.util.Collection;

/**
 * Сервис отправки сообщений
 *
 * @param <R> тип данных
 */
public interface Sender<R> {

    /**
     * Отправка сообщения
     *
     * @param data данные
     */
    void send(R data);

    /**
     * Отправка списка сообщений
     *
     * @param elements список данных
     */
    default void sendAll(Collection<R> elements) {
        elements.forEach(this::send);
    }
}
