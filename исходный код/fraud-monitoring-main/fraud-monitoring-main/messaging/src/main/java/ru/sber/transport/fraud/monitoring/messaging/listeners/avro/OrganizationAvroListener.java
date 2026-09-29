package ru.sber.transport.fraud.monitoring.messaging.listeners.avro;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sber.transport.fraud.monitoring.business.OrganizationsService;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.avro.OrganizationAvroData;
import ru.sber.transport.messages.corporate.avro.OrganizationMessage;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class OrganizationAvroListener implements Consumer<Message<OrganizationMessage>> {

    private final OrganizationsService organizationsService;

    @Override
    public void accept(Message<OrganizationMessage> raw) {
        organizationsService.createOrUpdate(new OrganizationAvroData(raw.getPayload()));
    }
}
