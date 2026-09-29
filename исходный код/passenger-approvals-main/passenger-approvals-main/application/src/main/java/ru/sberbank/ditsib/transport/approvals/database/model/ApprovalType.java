package ru.sberbank.ditsib.transport.approvals.database.model;

/**
 * Доступные типы согласований.
 */
public enum ApprovalType {
    
    UPDATE_TRIP_REQUEST,
    
    FINAL_TRIP,
    
    SHARED_RIDE_JOIN,
    /**
     * Заявка на поездку.
     */
    TRIP_REQUEST
}
