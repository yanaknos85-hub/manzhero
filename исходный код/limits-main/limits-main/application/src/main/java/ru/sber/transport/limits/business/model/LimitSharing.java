package ru.sber.transport.limits.business.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Данные распределения лимитов
 */
@Data
public class LimitSharing {

    /**
     * Идентификатор
     */
    private UUID id;

    /**
     * Сумма
     */
    private BigDecimal sum;

    /**
     * Остаток
     */
    private BigDecimal remains;

    /**
     * Тип транспорта
     */
    private String transportType;

    /**
     * Распределения по периодам
     */
    private List<LimitSharingPerPeriod> periods;

}
