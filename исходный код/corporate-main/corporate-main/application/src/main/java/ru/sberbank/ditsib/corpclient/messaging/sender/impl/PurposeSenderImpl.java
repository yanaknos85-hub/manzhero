package ru.sberbank.ditsib.corpclient.messaging.sender.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.database.model.TripPurpose;
import ru.sberbank.ditsib.corpclient.messaging.sender.PurposeSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.messaging.messages.TripPurposeMessage;

import java.util.Optional;

/**
 * Implementation of purpose sender.
 */
@Slf4j
@RequiredArgsConstructor
@Component
class PurposeSenderImpl implements PurposeSender {

    @Qualifier("purposeOutput")
    private final ObjectProvider<OutputBridge> purposeOutput;

    @Qualifier("purposeOutputSsl")
    private final ObjectProvider<OutputBridge> purposeOutputSsl;
    
    @Override
    public void send(@NonNull TripPurpose tripPurpose) {
        var organizationId = Optional.ofNullable(tripPurpose.getOrganization())
                                     .map(Organization::getId).orElse(null);
        var message = TripPurposeMessage.builder()
                                        .label(tripPurpose.getLabel())
                                        .id(tripPurpose.getId())
                                        .organization(organizationId)
                                        .deleted(!tripPurpose.isActive())
                                        .build();
        log.debug("Sending message with trip purpose: {}", tripPurpose);
        purposeOutput.ifAvailable(ob -> ob.send(message));
        purposeOutputSsl.ifAvailable(ob -> ob.send(message));
    }
    
}
