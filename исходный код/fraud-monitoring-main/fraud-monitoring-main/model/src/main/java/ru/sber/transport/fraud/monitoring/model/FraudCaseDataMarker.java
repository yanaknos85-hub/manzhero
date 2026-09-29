package ru.sber.transport.fraud.monitoring.model;

import java.util.UUID;

/**
 * Модель маркера кейса фрода для API.
 */
public interface FraudCaseDataMarker {

    /**
     * Идентификатор кейса фрода
     *
     * @return идентификатор кейса фрода
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

    /**
     * Вердикт ИИ
     *
     * @return вердикт ИИ
     */
    String getAiVerdict();

    /**
     * Флаг необходимости валидации
     *
     * @return true если требуется валидация
     */
    boolean isNeedValidation();

}