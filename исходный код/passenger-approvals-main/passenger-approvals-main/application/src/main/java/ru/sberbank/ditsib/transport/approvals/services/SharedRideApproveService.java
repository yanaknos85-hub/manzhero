package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.database.model.SharedRideJoinApproval;

import java.util.Optional;
import java.util.UUID;

/**
 * Service to approval logic for shared ride request
 */
public interface SharedRideApproveService extends ApproveService<SharedRideJoinApproval> {
    Optional<SharedRideJoinApproval> getByAddedRequestId(UUID addedRequestId);
}
