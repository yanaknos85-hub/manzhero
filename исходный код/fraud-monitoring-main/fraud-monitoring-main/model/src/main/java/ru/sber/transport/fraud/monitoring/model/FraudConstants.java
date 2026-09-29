package ru.sber.transport.fraud.monitoring.model;

import lombok.experimental.UtilityClass;

/**
 * Константы для работы с фрод-данными.
 */
@UtilityClass
public final class FraudConstants {

    /**
     * Значение вердикта ИИ, при котором подозрение по фроду считается снятым.
     */
    public static final String NOT_FRAUD_VERDICT = "NOT_FRAUD";

}
