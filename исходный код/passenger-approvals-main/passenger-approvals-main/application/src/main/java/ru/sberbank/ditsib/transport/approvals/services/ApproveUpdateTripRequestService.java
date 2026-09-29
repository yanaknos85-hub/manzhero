package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.database.model.UpdateTripRequestApproval;

import java.util.UUID;

/**
 * Service to approval logic for update trip request
 */
public interface ApproveUpdateTripRequestService extends ApproveService<UpdateTripRequestApproval> {
    UpdateTripRequestApproval findByUpdateIdOrCreate(UUID updateId);

    void cancelByRequestId(UUID requestId);

    /**
     * Don't use this method because there can be many approvals for one actionId
     *
     * @param actionId
     * @return
     */
    @Deprecated
    UpdateTripRequestApproval findOrCreate(UUID actionId);

}
