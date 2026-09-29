package ru.sberbank.ditsib.corpclient.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.corpclient.database.model.CargoType;
import ru.sberbank.ditsib.corpclient.dto.mapper.CargoTypeMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.CargoTypeSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.messaging.messages.CargoTypeMessage;

import java.util.UUID;

/**
 * Implementation of cargo type sender.
 */
@RequiredArgsConstructor
@Component
class CargoTypeSenderImpl implements CargoTypeSender {

    @Qualifier("cargoTypeOutput")
    private final ObjectProvider<OutputBridge> cargoTypeOutput;

    @Qualifier("cargoTypeOutputSsl")
    private final ObjectProvider<OutputBridge> cargoTypeOutputSsl;

    private final CargoTypeMapper mapper;

    @Override
    public void send(CargoType cargoType) {
        var message = mapper.toMessage(cargoType);
        cargoTypeOutput.ifAvailable(ob -> ob.send(message));
        cargoTypeOutputSsl.ifAvailable(ob -> ob.send(message));
    }

    @Override
    public void sendDeleted(UUID id) {
        if (id != null) {
            var message = CargoTypeMessage.builder()
                .deleted(true)
                .id(id)
                .build();
            cargoTypeOutput.ifAvailable(ob -> ob.send(message));
            cargoTypeOutputSsl.ifAvailable(ob -> ob.send(message));
        }
    }

}
