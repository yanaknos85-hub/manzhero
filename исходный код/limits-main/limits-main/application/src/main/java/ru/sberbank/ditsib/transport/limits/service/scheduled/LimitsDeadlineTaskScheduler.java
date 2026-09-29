package ru.sberbank.ditsib.transport.limits.service.scheduled;

/**
 * Планировщик задач по расписанию, связанных с КС
 */
public interface LimitsDeadlineTaskScheduler {
    
    /**
     * Задача шедулера по проверке всех заявкок по КС.
     */
    void checkRequestDeadlines();
}
