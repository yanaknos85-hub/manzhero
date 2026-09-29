package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.transport.approvals.database.model.Delegate;
import ru.sberbank.ditsib.transport.approvals.messaging.listeners.DelegateListener;
import ru.sberbank.ditsib.transport.approvals.messaging.message.DelegateMessage;
import ru.sberbank.ditsib.transport.approvals.services.DelegateService;
import ru.sberbank.ditsib.transport.approvals.services.TripApproverService;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

/**
 * Реализация слушателя делегатов.
 */
@RequiredArgsConstructor
public class DelegateListenerImpl implements DelegateListener {
    
    private final DelegateService delegateService;
    
    private final TripApproverService approverService;
    
    @Override
    public void handle(DelegateMessage message) {
        var delegate = delegateService.get(message.getId()).orElse(new Delegate());
        if (message.isDeleted()) {
            delegateService.delete(delegate);
        } else {
            delegate.setId(message.getId());
            delegate.setDelegateId(message.getDelegateId());
            delegate.setStartDate(message.getStartDate());
            delegate.setEndDate(message.getEndDate());
            delegate.setSupervisorId(message.getSupervisorId());
            delegate.setTransportType(TransportTypeEnum.fromId(message.getTransportTypeId()).map(TransportTypeEnum::name).orElse(null));
            delegateService.save(delegate);
        }
        if (delegate.getId() != null) {
            approverService.onDelegateChanged(delegate.getSupervisorId());
        }
        
    }
}
