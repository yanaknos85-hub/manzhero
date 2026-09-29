package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sberbank.ditsib.transport.approvals.messaging.listeners.TripRequestListener;
import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestMessage;
import ru.sberbank.ditsib.transport.approvals.services.RequestApprovalService;

/**
 * Реализация слушателя заявок на поездку.
 */
@RequiredArgsConstructor
@Slf4j
public class TripRequestListenerImpl implements TripRequestListener {

    private final RequestApprovalService requestApprovalService;

    @Override
    public void handle(RequestMessage message) {
        requestApprovalService.handleRequestMessage(message);
    }
}
