package ru.sber.transport.grpc.test;

import java.util.concurrent.TimeUnit;

/**
 * Объект, подлежащий очистке.
 */
public interface CleanupTarget {

    /**
     * Выключение объекта.
     */
    void shutdown();

    /**
     * Ожидание завершения работы.
     *
     * @param timeout таймаут.
     * @param timeUnit тип данных времени.
     * @return <code>true</code> если завершено успешно.
     * @throws InterruptedException выполнение было прервано.
     */
    boolean awaitTermination(long timeout, TimeUnit timeUnit) throws InterruptedException;

    /**
     * Проверка, выключен ли серис.
     *
     * @return выключено.
     */
    boolean isTerminated();
    
}
