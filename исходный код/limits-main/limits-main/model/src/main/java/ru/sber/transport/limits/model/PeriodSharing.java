package ru.sber.transport.limits.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Распределение на период
 */
public interface PeriodSharing {

    /**
     * Получить идентификатор распределения на период
     * @return идентификатор распределения на период
     */
    UUID id();

    /**
     * Возвращает остаток для данного периода
     *
     * @return остаток
     */
    BigDecimal remains();

}
