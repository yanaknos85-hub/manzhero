package ru.sber.transport.corporate.messaging.senders;

import ru.sber.transport.corporate.model.Delegate;

/**
 * Отправитель данных делегатов
 */
public interface DelegateSender {

    /**
     * Отправка делегата
     *
     * @param data Делегат
     */
    void send(Delegate data);

}
