package ru.sber.transport.fraud.monitoring.messaging.listeners;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sber.transport.fraud.monitoring.business.PositionsService;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.PositionData;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class PositionListener implements Consumer<Message<PositionMessage>> {

    private final PositionsService positionsService;

    @Override
    public void accept(Message<PositionMessage> raw) {
        positionsService.createOrUpdate(new PositionData(raw.getPayload()));
    }
}
