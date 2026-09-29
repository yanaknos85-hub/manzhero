package ru.sber.transport.fraud.monitoring.messaging.listeners.model.avro;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.fraud.monitoring.model.Organization;
import ru.sber.transport.messages.corporate.avro.OrganizationMessage;

/**
 * Данные организации
 */
@RequiredArgsConstructor
public final class OrganizationAvroData implements Organization {

    @Delegate
    private final OrganizationMessage delegate;

}
