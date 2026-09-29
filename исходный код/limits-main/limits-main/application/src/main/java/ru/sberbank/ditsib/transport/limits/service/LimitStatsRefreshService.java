package ru.sberbank.ditsib.transport.limits.service;

import java.util.concurrent.CompletableFuture;

/**
 *  Сервис для обновления состояния материализованных view.
 */
public interface LimitStatsRefreshService {

    /**
     * Запуск обновления.
     *
     * @param statsClass класс материализованной view.
     * @return результат запуска. <code>false</code> если view уже в процессе обновления и новый процесс не был запущен.
     */
    CompletableFuture<Boolean> refresh(Class<?> statsClass);

}
