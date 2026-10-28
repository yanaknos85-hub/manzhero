package ru.sber.transport.trip.registries.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import ru.sber.transport.database.trip_registries.tables.records.RegistryRecord;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.trip.registry.messaging.RegistryMessage;
import ru.sber.transport.trip.registries.messaging.senders.RegistrySender;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class RegistrySenderImpl implements RegistrySender {

    @Qualifier("tripRegistryOutput")
    private final ObjectProvider<OutputBridge> tripRegistryOutput;

    @Qualifier("tripRegistryOutputSsl")
    private final ObjectProvider<OutputBridge> tripRegistryOutputSsl;

    @Override
    public void send(RegistryRecord registry) {
        var message = toMessage(registry);
        tripRegistryOutput.ifAvailable(ob -> ob.send(message, Map.of(KafkaHeaders.KEY, message.getId())));
        // TODO реализовать корректную конфигурацию подключения к кластеру Kafka с SSL
       // tripRegistryOutputSsl.ifAvailable(ob -> ob.send(message, Map.of(KafkaHeaders.KEY, message.getId())));
    }
    
    RegistryMessage toMessage(RegistryRecord registry) {
        return new RegistryMessage(
                registry.getRegistryId(), registry.getRegistryState().toString(), registry.getActId(),
                registry.getLockingIndicator(), registry.getUpdStatus(), registry.getActNumberError(), registry.getServiceType());
    }
}
