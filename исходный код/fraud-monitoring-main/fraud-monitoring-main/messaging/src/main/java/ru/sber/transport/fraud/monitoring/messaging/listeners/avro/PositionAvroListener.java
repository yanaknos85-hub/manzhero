package ru.sber.transport.fraud.monitoring.messaging.listeners.avro;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sber.transport.fraud.monitoring.business.PositionsService;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.avro.PositionAvroData;
import ru.sber.transport.messages.corporate.avro.PositionMessage;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class PositionAvroListener implements Consumer<Message<PositionMessage>> {

    private final PositionsService positionsService;

    @Override
    public void accept(Message<PositionMessage> raw) {
        positionsService.createOrUpdate(new PositionAvroData(raw.getPayload()));
    }
}
