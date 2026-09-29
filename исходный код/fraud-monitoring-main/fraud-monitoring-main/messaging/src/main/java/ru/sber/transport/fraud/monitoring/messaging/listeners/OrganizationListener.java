package ru.sber.transport.fraud.monitoring.messaging.listeners;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sber.transport.fraud.monitoring.business.OrganizationsService;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.OrganizationData;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class OrganizationListener implements Consumer<Message<OrganizationMessage>> {

    private final OrganizationsService organizationsService;

    @Override
    public void accept(Message<OrganizationMessage> raw) {
        organizationsService.createOrUpdate(new OrganizationData(raw.getPayload()));
    }
}
