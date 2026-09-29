package ru.sberbank.ditsib.transport.limits.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.transport.limits.model.limit.Month;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;
import ru.sberbank.ditsib.transport.limits.model.limit.Quarter;

import java.util.List;

/**
 * Типы распределений лимита.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum LimitSharingType {

    /**
     * Месячное распределение.
     */
    MONTHLY(Month.class, List.of(Month.values())),

    /**
     * Квартальное распределение.
     */
    QUARTER(Quarter.class, List.of(Quarter.values())),

    /**
     * Процентное распределение.
     */
    PERCENTS(Month.class, List.of(Month.values()));

    private final Class<? extends Period> periodClass;

    private final List<? extends Period> periods;
}