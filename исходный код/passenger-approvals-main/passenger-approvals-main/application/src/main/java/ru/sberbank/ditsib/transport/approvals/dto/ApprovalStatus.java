package ru.sberbank.ditsib.transport.approvals.dto;

/**
 * Статусы согласования
 */
public enum ApprovalStatus {

    /**
     * Новое
     */
    NEW,

    /**
     * Изменено
     */
    EDITED,

    /**
     * Подтверждено
     */
    APPROVED,

    /**
     * Отклонено
     */
    DECLINED,

    /**
     * Отменено
     */
    CANCELLED
}
