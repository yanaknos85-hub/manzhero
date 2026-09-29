package ru.sber.transport.limits.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Расходы
 */
public interface Spending {

    /**
     * Получить идентификатор расхода
     *
     * @return идентификатор расхода
     */
    UUID id();

    /**
     * Получить зарезервированную сумму
     *
     * @return зарезервированная сумма
     */
    BigDecimal reserved();

    /**
     * Получить ссылку на распределение на период
     *
     * @return идентификатор распределения на период
     */
    UUID periodSharing();

    /**
     * Получить статус расхода
     *
     * @return статус расхода
     */
    ReserveStatus status();

}
