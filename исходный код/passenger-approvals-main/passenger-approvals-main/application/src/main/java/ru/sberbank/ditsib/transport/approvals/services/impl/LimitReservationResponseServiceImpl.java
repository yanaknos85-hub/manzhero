package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.approvals.database.dao.LimitReservationResponseRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.LimitReservationResponse;
import ru.sberbank.ditsib.transport.approvals.messaging.listeners.constants.Limits;
import ru.sberbank.ditsib.transport.approvals.services.LimitReservationResponseService;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of limit reservation response service.
 */
@RequiredArgsConstructor
@Service
@Transactional
public class LimitReservationResponseServiceImpl implements LimitReservationResponseService {
    private final LimitReservationResponseRepository limitRepository;

    @Override
    public void save(UUID tripRequestId, String status) {
        final LimitReservationResponse limit =
            limitRepository.findByTripRequestId(tripRequestId).orElse(new LimitReservationResponse());
        limit.setTripRequestId(tripRequestId);
        limit.setStatus(status);
        limitRepository.save(limit);
    }

    @Override
    public Optional<LimitReservationResponse> findByTripRequestId(UUID tripRequestId) {
        return limitRepository.findByTripRequestId(tripRequestId);
    }

    @Override
    public LimitReservationResponse findLimitReservedByEmployeeId(UUID requestId) {
        LimitReservationResponse limitReservationResponse = findByTripRequestId(requestId).orElse(null);
        if ((null == limitReservationResponse) ||
            (!Limits.RESERVED_FROM_EMPLOYEE.equals(limitReservationResponse.getStatus()))) {
            return null;
        }
        return limitReservationResponse;
    }
}
