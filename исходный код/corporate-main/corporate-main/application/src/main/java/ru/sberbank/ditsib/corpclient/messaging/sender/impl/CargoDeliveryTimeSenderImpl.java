package ru.sberbank.ditsib.corpclient.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.corpclient.database.model.CargoDeliveryTime;
import ru.sberbank.ditsib.corpclient.mapper.CargoDeliveryMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.CargoDeliveryTimeSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.messaging.messages.CargoDeliveryTimeMessage;

import java.util.UUID;

/**
 * Implementation of cargo type sender.
 */
@RequiredArgsConstructor
@Component
class CargoDeliveryTimeSenderImpl implements CargoDeliveryTimeSender {

    @Qualifier("cargoDeliveryTimeOutput")
    private final ObjectProvider<OutputBridge> cargoDeliveryTimeOutput;

    @Qualifier("cargoDeliveryTimeOutputSsl")
    private final ObjectProvider<OutputBridge> cargoDeliveryTimeOutputSsl;

    private final CargoDeliveryMapper mapper;

    @Override
    public void send(CargoDeliveryTime cargoDeliveryTime) {
        var message = mapper.toMessage(cargoDeliveryTime);
        cargoDeliveryTimeOutput.ifAvailable(ob -> ob.send(message));
        cargoDeliveryTimeOutputSsl.ifAvailable(ob -> ob.send(message));
    }

    @Override
    public void sendDeleted(UUID id) {
        if (id != null) {
            var message = CargoDeliveryTimeMessage.builder()
                .deleted(true)
                .id(id)
                .build();
            cargoDeliveryTimeOutput.ifAvailable(ob -> ob.send(message));
            cargoDeliveryTimeOutputSsl.ifAvailable(ob -> ob.send(message));
        }
    }

}
