package ru.sber.transport.messaging;

/**
 * Интерфейс сообщений с идентификатором.
 *
 * @param <I> тип идентификатора.
 */
public interface Message<I> extends ru.sberbank.ditsib.transport.messaging.Message<I> {
    
    /**
     * Идентификатор сообщения.
     *
     * @return идентификатор сообщения.
     */
    I getId();
    
}
