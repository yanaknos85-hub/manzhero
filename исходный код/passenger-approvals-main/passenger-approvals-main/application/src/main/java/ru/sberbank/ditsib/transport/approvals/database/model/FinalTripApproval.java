package ru.sberbank.ditsib.transport.approvals.database.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Утверждение финального маршрута
 */
@Entity
@Table(name = "final_trip_approvals", schema = "approvals")
@Setter
@Getter
public class FinalTripApproval extends BaseRequestApproval {
    
    /**
     * Является ли поездка междугородней
     */
    @Column(name = "is_suburb_trip")
    private boolean isSuburbTrip = false;
    
    /**
     * Дата и время утверждения поездки
     */
    @Column(name = "approval_date")
    private LocalDateTime approvalDate = null;
    
    @Override
    public ApprovalType getType() {
        return ApprovalType.FINAL_TRIP;
    }
}
