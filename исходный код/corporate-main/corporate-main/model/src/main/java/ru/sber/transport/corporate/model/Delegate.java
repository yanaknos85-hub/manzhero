package ru.sber.transport.corporate.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Модель делегата
 */
public interface Delegate {

    /**
     * Получить идентификатор делегирования
     *
     * @return идентификатор делегирования
     */
    UUID id();

    /**
     * Получить идентификатор делегата
     *
     * @return идентификатор делегата
     */
    UUID delegateId();

    /**
     * Получить идентификатор руководителя
     *
     * @return идентификатор руководителя
     */
    UUID supervisorId();

    /**
     * Получить дату начала действия делегирования
     *
     * @return дата начала действия делегирования
     */
    LocalDate startDate();

    /**
     * Получить дату окончания действия делегирования
     *
     * @return дата окончания действия делегирования
     */
    LocalDate endDate();

    /**
     * Получить тип делегирования
     *
     * @return тип делегирования
     */
    String type();

    /**
     * Получить статус делегирования
     *
     * @return статус делегирования
     */
    boolean deleted();

}
