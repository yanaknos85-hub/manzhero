package ru.sberbank.ditsib.transport.approvals.database.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Доступные статусы согласований.
 */
@Getter
@AllArgsConstructor
public enum Status {
    
    /**
     * Новое согласование.
     */
    NEW,
    
    /**
     * Тело согласования изменено.
     */
    EDITED,
    
    /**
     * Согласовано.
     */
    APPROVED,
    
    /**
     * Отклонено.
     */
    DECLINED,
    
    /**
     * Согласование отменено.
     */
    CANCELLED
}
