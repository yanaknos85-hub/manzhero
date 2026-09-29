package ru.sberbank.ditsib.transport.approvals.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Типы согласований")
public enum Type {
    
    UPDATE_TRIP_REQUEST,
    
    FINAL_TRIP,
    
    SHARED_RIDE_JOIN,
    /**
     * Заявка на поездку.
     */
    TRIP_REQUEST
}
