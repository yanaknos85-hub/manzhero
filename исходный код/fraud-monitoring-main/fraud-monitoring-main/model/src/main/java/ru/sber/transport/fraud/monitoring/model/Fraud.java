package ru.sber.transport.fraud.monitoring.model;

import java.util.UUID;

/**
 * Интерфейс фрода
 */
public interface Fraud {

    /**
     * Идентификатор нарушения (фрода)
     *
     * @return идентификатор нарушения (фрода)
     */
    UUID getId();

    /**
     * Идентификатор заявки
     *
     * @return идентификатор заявки
     */
    UUID getRequestId();

    /**
     * Комментарий к фродовой заявке
     *
     * @return комментарий к фродовой заявке
     */
    String getComment();

    /**
     * Тип мошенничества
     *
     * @return тип мошенничества
     */
    String getFraudType();

    /**
     * Название сервиса-источника
     *
     * @return название сервиса-источника
     */
    String getSource();
}
