package ru.sberbank.ditsib.corpclient.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.corpclient.dto.PersonalCarDTO;
import ru.sberbank.ditsib.corpclient.mapper.PersonalCarMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.PersonalCarSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

/**
 * Implementation of personal car sender.
 */
@Component
@RequiredArgsConstructor
class PersonalCarSenderImpl implements PersonalCarSender {

    @Qualifier("personalCarsOutput")
    private final ObjectProvider<OutputBridge> personalCarsOutput;

    @Qualifier("personalCarsOutputSsl")
    private final ObjectProvider<OutputBridge> personalCarsOutputSsl;
    
    private final PersonalCarMapper mapper;
    
    @Override
    public void send(PersonalCarDTO car, boolean deleted) {
        var data = mapper.toMessage(car);
        data.setDeleted(deleted);
        personalCarsOutput.ifAvailable(ob -> ob.send(data));
        personalCarsOutputSsl.ifAvailable(ob -> ob.send(data));
    }
}
