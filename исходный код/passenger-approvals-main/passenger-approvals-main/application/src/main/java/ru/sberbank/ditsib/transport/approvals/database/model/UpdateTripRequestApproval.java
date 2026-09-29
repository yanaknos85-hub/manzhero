package ru.sberbank.ditsib.transport.approvals.database.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "update_trip_request_approvals", schema = "approvals")
@Setter
@Getter
public class UpdateTripRequestApproval extends BaseRequestApproval {
    @Column(name = "update_id", nullable = false)
    private UUID updateId;
    
    @Override
    public ApprovalType getType() {
        return ApprovalType.UPDATE_TRIP_REQUEST;
    }
}
