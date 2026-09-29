package ru.sberbank.ditsib.corpclient.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.corpclient.database.model.TransportOrg;
import ru.sberbank.ditsib.corpclient.mapper.TransportOrgMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.TransportOrgSender;

/**
 * Implementation of transport by organization.
 */
@RequiredArgsConstructor
@Component
class TransportOrgSenderImpl implements TransportOrgSender {

    @Qualifier("transportOrgOutput")
    private final ObjectProvider<OutputBridge> transportOrgOutput;

    @Qualifier("transportOrgOutputSsl")
    private final ObjectProvider<OutputBridge> transportOrgOutputSsl;

    private final TransportOrgMapper mapper;

    @Override
    public void send(TransportOrg transportOrg, boolean deleted) {
        if (!"YANDEX".equals(transportOrg.getTransportType())) {
            var message = mapper.toMessage(transportOrg, deleted);
            transportOrgOutput.ifAvailable(ob -> ob.send(message));
            transportOrgOutputSsl.ifAvailable(ob -> ob.send(message));
        }
    }
}
