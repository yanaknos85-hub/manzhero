package ru.sberbank.ditsib.transport.messaging;

/**
 * Интерфейс сообщений с идентификатором.
 *
 * @param <I> тип идентификатора.
 * @deprecated Интерфейс перемещен в ru.sber.transport.messaging. Удалить интерфейс когда версия ниже 3.11 не будет использоваться.
 */
@Deprecated(since = "3.11", forRemoval = true)
public interface Message<I> {
    
    /**
     * Идентификатор сообщения.
     *
     * @return идентификатор сообщения.
     */
    I getId();
    
}
