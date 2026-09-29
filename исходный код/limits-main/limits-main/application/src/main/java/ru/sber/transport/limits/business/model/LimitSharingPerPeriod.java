package ru.sber.transport.limits.business.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Данные распределения лимитов по периоду
 */
@Data
public class LimitSharingPerPeriod {

    /**
     * Идентификатор
     */
    private UUID id;

    /**
     * Идентификатор распределения
     */
    private UUID limitSharingId;

    /**
     * Сумма
     */
    private BigDecimal sum;

    /**
     * Остаток
     */
    private BigDecimal balance;

    /**
     * Баланс
     */
    private BigDecimal additional;

    /**
     * Период
     */
    private Period period;

    /**
     * Идентификатор автора
     */
    private UUID authorId;

    /**
     * Признак был ли перемещен лимит на следующий месяц
     */
    private boolean movedToNext;

    /**
     * Признак был ли оповещен об изменении лимита
     */
    private OffsetDateTime noticedAt;
}
