package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.database.model.LimitReservationResponse;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with limit reservation responses.
 */
public interface LimitReservationResponseService {
    void save(UUID tripRequestId, String status);

    Optional<LimitReservationResponse> findByTripRequestId(UUID tripRequestId);

    /**
     *
     * @param requestId id of trip request
     * @return {@link LimitReservationResponse} if limit reserved for requestId and have status
     * "RESERVED_FROM_EMPLOYEE"
     */
    LimitReservationResponse findLimitReservedByEmployeeId(UUID requestId);
}
