package ru.sberbank.ditsib.transport.limits.service;

import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPerPeriod;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Интерфейс распределения лимитов по периодам.
 *
 * @param <T> тип периода.
 */
public interface LimitsSharingDistributor<T extends Period> {

    /**
     * Распределить лимит по периодам.
     *
     * @param limit лимит
     * @param totalSum сумма по лимиту
     * @param limitSharingPerPeriodList список периодов
     * @param sumMap суммы
     * @param imitation имитация
     */
    void distributeSharingPerPeriodOtherYears(Limit limit,
                                              BigDecimal totalSum,
                                              Map<? extends T, LimitSharingPerPeriod> limitSharingPerPeriodList,
                                              Map<T, BigDecimal> sumMap,
                                              boolean imitation);

    /**
     * Распределить лимит по периодам.
     *
     * @param limit лимит
     * @param totalSum сумма по лимиту
     * @param limitSharingPerPeriodList список периодов
     * @param sumMap суммы
     * @param imitation имитация
     */
    void distributeLimitSharingCurrentYear(Limit limit,
                                           BigDecimal totalSum,
                                              Map<? extends T, LimitSharingPerPeriod> limitSharingPerPeriodList,
                                              Map<T, BigDecimal> sumMap,
                                              boolean imitation);

    /**
     * Получить тип распределения.
     *
     * @return тип распределения
     */
    LimitSharingType type();

}
