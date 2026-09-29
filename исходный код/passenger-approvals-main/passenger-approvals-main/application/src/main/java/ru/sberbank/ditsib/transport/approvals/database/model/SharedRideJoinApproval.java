package ru.sberbank.ditsib.transport.approvals.database.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "shared_ride_approvals", schema = "approvals")
@Setter
@Getter
@ToString
public class SharedRideJoinApproval extends BaseRequestApproval {
    /**
     * Присоединяемоя поездка
     */
    @Column(name = "add_request_id")
    private UUID addRequestId;
    
    @Override
    public ApprovalType getType() {
        return ApprovalType.SHARED_RIDE_JOIN;
    }
}
