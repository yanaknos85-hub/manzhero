package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sberbank.ditsib.transport.approvals.mappers.TripPurposeMapper;
import ru.sberbank.ditsib.transport.approvals.messaging.listeners.TripPurposeListener;
import ru.sberbank.ditsib.transport.approvals.messaging.message.TripPurposeMessage;
import ru.sberbank.ditsib.transport.approvals.services.TripPurposeService;

/**
 * Реализация слушателя создания/удаления цели.
 */
@RequiredArgsConstructor
@Slf4j
public class TripPurposeListenerImpl implements TripPurposeListener {
    
    private final TripPurposeMapper mapper;
    
    private final TripPurposeService tripPurposeService;

    @Override
    public void handle(TripPurposeMessage message) {
        tripPurposeService.save(mapper.toModel(message));
    }
}
