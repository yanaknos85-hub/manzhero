package ru.sberbank.ditsib.corpclient.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.corpclient.database.model.Position;
import ru.sberbank.ditsib.corpclient.dto.PositionDTO;
import ru.sberbank.ditsib.corpclient.mapper.PositionMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.PositionSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

import java.util.Collection;

/**
 * Implementation of position sender.
 */
@Slf4j
@RequiredArgsConstructor
@Component("oldPositionSender")
class PositionSenderImpl implements PositionSender {

    @Qualifier("positionOutput")
    private final ObjectProvider<OutputBridge> positionOutput;

    @Qualifier("positionOutputSsl")
    private final ObjectProvider<OutputBridge> positionOutputSsl;

    @Qualifier("positionOutputAvro")
    private final ObjectProvider<OutputBridge> positionOutputAvro;

    private final PositionMapper mapper;
    
    @Override
    public void send(PositionDTO position, boolean deleted) {
        var data = mapper.toMessage(position);
        data.setDeleted(deleted);
        positionOutput.ifAvailable(ob -> ob.send(data));
        positionOutputSsl.ifAvailable(ob -> ob.send(data));
        positionOutputAvro.ifAvailable(ob -> ob.send(mapper.toMessageAvro(position)));
    }

    public void send(Collection<Position> positions) {
        for (var value : positions) {
            var message = mapper.toMessage(value);
            positionOutput.ifAvailable(ob -> ob.send(message));
            positionOutputSsl.ifAvailable(ob -> ob.send(message));
        }
    }
}
