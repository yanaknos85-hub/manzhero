package ru.sber.transport.fraud.monitoring.model;

/**
 * Модель запроса на решение по разбирательству фрода
 */
public interface DecisionRequestData {

    /**
     * Решение сотрудника УТО
     *
     * @return решение сотрудника УТО
     */
    String getDecision();

    /**
     * Комментарий сотрудника относительно решения
     *
     * @return комментарий сотрудника
     */
    String getReason();
}