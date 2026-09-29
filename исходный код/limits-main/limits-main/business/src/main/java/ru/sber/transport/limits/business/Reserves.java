package ru.sber.transport.limits.business;

import ru.sber.transport.limits.business.exceptions.ReserveNotFoundException;
import ru.sber.transport.limits.business.exceptions.ReserveNotSufficientException;
import ru.sber.transport.limits.business.exceptions.ServiceNotAvailableException;
import ru.sber.transport.limits.business.exceptions.TypeNotAvailableException;
import ru.sber.transport.limits.model.Reserve;

import java.util.UUID;

/**
 * Интерфейс сервиса работы с резервами.
 */
public interface Reserves {

    /**
     * Добавляет резерв.
     *
     * @param data данные резерва.
     * @throws ReserveNotSufficientException если резерв не удалось совершить из-за недостатка лимита.
     * @throws TypeNotAvailableException если резерв не удалось совершить из-за недоступности типа.
     * @throws ServiceNotAvailableException если резерв не удалось совершить из-за недоступности услуги.
     */
    void reserve(Reserve data) throws ReserveNotSufficientException, TypeNotAvailableException, ServiceNotAvailableException;

    /**
     * Отменяет резерв.
     *
     * @param uuid идентификатор операции.
     * @throws ReserveNotFoundException если резерв не найден.
     */
    void cancel(UUID uuid) throws ReserveNotFoundException;

    /**
     * Подтверждает резерв.
     *
     * @param uuid идентификатор операции.
     * @throws ReserveNotFoundException если резерв не найден.
     */
    void confirm(UUID uuid) throws ReserveNotFoundException;
}
