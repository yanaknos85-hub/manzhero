package ru.sberbank.ditsib.transport.approvals.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Entity
@Table(name = "trip_request_approvals", schema = "approvals")
@Setter
@Getter
@ToString
public class TripRequestApproval extends BaseRequestApproval {
    /**
     * shared ride id
     */
    @Column(name = "shared_ride_id")
    private UUID sharedRideId;

    /**
     * shared ride id
     */
    @Column(name = "shared_ride_owner")
    private Boolean sharedRideOwner;
    
    /**
     * Является ли поездка междугородней
     */
    @Column(name = "is_suburb_trip")
    private boolean isSuburbTrip = false;

    @Override
    public ApprovalType getType() {
        return ApprovalType.TRIP_REQUEST;
    }
}
