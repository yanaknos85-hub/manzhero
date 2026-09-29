package ru.sber.transport.limits.business;

import lombok.NonNull;

import java.util.UUID;

/**
 * Бизнес-кейсы по работе с распределениями за период.
 */
public interface LimitSharingPerPeriods {

    /**
     * Проверка остатков.
     *
     * @param id идентификатор распределения.
     */
    void checkRemains(@NonNull UUID id);
}
