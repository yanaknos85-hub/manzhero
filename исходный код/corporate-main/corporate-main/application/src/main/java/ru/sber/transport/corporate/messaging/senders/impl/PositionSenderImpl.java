package ru.sber.transport.corporate.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.business.model.Position;
import ru.sber.transport.corporate.messaging.senders.PositionSender;
import ru.sber.transport.corporate.messaging.senders.mappers.PositionMessageMapper;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

import java.util.HashMap;

@RequiredArgsConstructor
@Component
class PositionSenderImpl implements PositionSender {

    @SuppressWarnings("Autowired")
    @Qualifier("positionOutput")
    private final ObjectProvider<OutputBridge> positionOutputBridge;

    @SuppressWarnings("Autowired")
    @Qualifier("positionOutputSsl")
    private final ObjectProvider<OutputBridge> positionOutputSslBridge;

    @SuppressWarnings("Autowired")
    @Qualifier("positionOutputAvro")
    private final ObjectProvider<OutputBridge> positionOutputAvroBridge;

    private final PositionMessageMapper mapper;

    @Override
    public void send(Position source) {
        final var headers = new HashMap<String, Object>();
        final var syncId = source.getSyncId();
        if (syncId != null) {
            headers.put("syncId", syncId);
        }
        final var data = mapper.toMessage(source);
        positionOutputBridge.ifAvailable(ob -> ob.send(data, headers));
        positionOutputSslBridge.ifAvailable(ob -> ob.send(data, headers));
        positionOutputAvroBridge.ifAvailable(ob -> ob.send(mapper.toMessageAvro(source), headers));
    }
}
