package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sberbank.ditsib.transport.approvals.database.model.TripRequestApproval;
import ru.sberbank.ditsib.transport.approvals.messaging.listeners.LimitReservationResponseListener;
import ru.sberbank.ditsib.transport.approvals.messaging.listeners.constants.Limits;
import ru.sberbank.ditsib.transport.approvals.messaging.message.LimitActionResultMessage;
import ru.sberbank.ditsib.transport.approvals.services.ApproveService;
import ru.sberbank.ditsib.transport.approvals.services.LimitReservationResponseService;

import java.util.UUID;

/**
 * Implementation of limit reservation response listener.
 */
@RequiredArgsConstructor
@Slf4j
public class LimitActionResponseListenerImpl implements LimitReservationResponseListener {
    private final ApproveService<TripRequestApproval> tripApproveService;
    private final LimitReservationResponseService limitReservationResponseService;

    @Override
    public void handleLimitReservationResponse(LimitActionResultMessage message) {
        if (!Limits.RESERVED_FROM_EMPLOYEE.equals(message.getLimitReservationStatus())) {
            return;
        }
        final UUID tripRequestId = message.getTripRequestId();
        if (limitReservationResponseService.findByTripRequestId(tripRequestId).isEmpty()) {
            limitReservationResponseService.save(tripRequestId, message.getLimitReservationStatus());
        }
        tripApproveService.handlingAutoApproving(tripRequestId);
    }
}
