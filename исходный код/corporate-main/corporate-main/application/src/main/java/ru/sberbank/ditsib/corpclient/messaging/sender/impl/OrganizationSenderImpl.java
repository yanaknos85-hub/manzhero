package ru.sberbank.ditsib.corpclient.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.mapper.OrganizationMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.OrganizationSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

/**
 * Implementation of organization sender.
 */
@RequiredArgsConstructor
@Component("oldOrganizationSender")
@Transactional
class OrganizationSenderImpl implements OrganizationSender {

    @Qualifier("organizationsOutput")
    private final ObjectProvider<OutputBridge> organizationsOutput;

    @Qualifier("organizationsOutputSsl")
    private final ObjectProvider<OutputBridge> organizationsOutputSsl;

    @Qualifier("organizationsOutputAvro")
    private final ObjectProvider<OutputBridge> organizationsOutputAvro;
    
    private final OrganizationMapper mapper;
    
    @Override
    public void send(Organization organization) {
        var message = mapper.toMessage(organization);
        organizationsOutput.ifAvailable(ob -> ob.send(message));
        organizationsOutputSsl.ifAvailable(ob -> ob.send(message));
        organizationsOutputAvro.ifAvailable(ob -> ob.send(mapper.toMessageAvro(organization)));
    }
}
