package ru.sber.transport.limits.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Модель данных для резерва.
 */
public interface Reserve {

    /**
     * Возвращает стоимость.
     *
     * @return стоимость.
     */
    BigDecimal cost();

    /**
     * Возвращает идентификатор.
     *
     * @return идентификатор.
     */
    UUID id();

    /**
     * Возвращает услугу.
     *
     * @return услуга.
     */
    String service();

    /**
     * Возвращает тип.
     *
     * @return тип.
     */
    String type();

    /**
     * Возвращает идентификатор потребителя.
     *
     * @return идентификатор потребителя.
     */
    UUID consumerId();

    /**
     * Возвращает дату предоставления услуги.
     *
     * @return дата.
     */
    OffsetDateTime date();
}
