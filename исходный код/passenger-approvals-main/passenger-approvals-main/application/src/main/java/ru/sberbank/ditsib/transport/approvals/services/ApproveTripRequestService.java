package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.database.model.TripRequestApproval;

import java.util.Optional;
import java.util.UUID;

public interface ApproveTripRequestService extends ApproveService<TripRequestApproval> {
    
    Optional<TripRequestApproval> findSharedRideOwnerApprove(UUID sharedRideId);
}
