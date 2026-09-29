package ru.sber.transport.fraud.monitoring.messaging.listeners.model;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.fraud.monitoring.model.Organization;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

/**
 * Данные организации
 */
@RequiredArgsConstructor
public final class OrganizationData implements Organization {

    @Delegate
    private final OrganizationMessage delegate;

    @Override
    public long getDigitId() {
        return delegate.getDigitId();
    }
}
