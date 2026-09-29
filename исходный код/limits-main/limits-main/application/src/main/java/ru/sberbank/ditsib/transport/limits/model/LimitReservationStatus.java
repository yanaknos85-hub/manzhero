package ru.sberbank.ditsib.transport.limits.model;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Статусы резервирования лимита
 */
public enum LimitReservationStatus {

    /**
     * Неправильная сумма
     */
    SUM_INCORRECT,

    /**
     * Зарезервировано от сотрудника
     */
    RESERVED_FROM_EMPLOYEE,

    /**
     * Зарезервировано от департамента
     */
    RESERVED_FROM_DEPARTMENT,

    /**
     * Лимит не найден
     */
    LIMIT_NOT_FOUND,

    /**
     * Лимит недостаточен
     */
    LIMIT_NOT_SUFFICIENT,

    /**
     * Лимит отменен
     */
    LIMIT_CANCELLED,

    /**
     * Лимит потрачен
     */
    LIMIT_SPENT,

    /**
     * Сотрудник не найден
     */
    ERROR_EMPLOYEE_NOT_FOUND,

    /**
     * Сотрудник не авторизован
     */
    ERROR_NOT_AUTHORIZED,

    /**
     * Ошибка типа действия
     */
    ACTION_TYPE_ERROR,

    /**
     * Фиксация траты не удалась
     */
    LIMIT_SPENT_FAILED,

    /**
     * Отмена резервирования не удалась
     */
    LIMIT_CANCEL_FAILED,

    /**
     * Резервирование не удалась
     */
    LIMIT_RESERVATION_FAILED,

    /**
     * Резервирование удалось
     */
    LIMIT_RESERVED;

    /**
     * Получить список провальных статусов
     *
     * @return список провальных статусов
     */
    public static List<LimitReservationStatus> getFailStatuses() {
        return asList(LIMIT_NOT_FOUND, LIMIT_NOT_SUFFICIENT, LIMIT_SPENT_FAILED, SUM_INCORRECT);
    }
}