package ru.sberbank.ditsib.corpclient.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.corpclient.database.model.DelegateRecord;
import ru.sberbank.ditsib.corpclient.mapper.DelegateMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.DelegateSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

/**
 * Реализация отправления данных о делегате.
 */
@RequiredArgsConstructor
@Component
class DelegateSenderImpl implements DelegateSender {

    private final DelegateMapper mapper;

    @Qualifier("delegateOutput")
    private final ObjectProvider<OutputBridge> delegateOutput;

    @Qualifier("delegateOutputSsl")
    private final ObjectProvider<OutputBridge> delegateOutputSsl;

    @Qualifier("delegateOutputAvro")
    private final ObjectProvider<OutputBridge> delegateOutputAvro;

    @Override
    public void send(DelegateRecord delegateRecord) {
        var message = mapper.toMessage(delegateRecord);
        delegateOutput.ifAvailable(ob -> ob.send(message));
        delegateOutputSsl.ifAvailable(ob -> ob.send(message));
        delegateOutputAvro.ifAvailable(ob -> ob.send(mapper.toMessageAvro(delegateRecord)));
    }

    @Override
    public void sendDelete(DelegateRecord delegateRecord) {
        var message = mapper.toMessage(delegateRecord, true);
        delegateOutput.ifAvailable(ob -> ob.send(message));
        delegateOutputSsl.ifAvailable(ob -> ob.send(message));
        delegateOutputAvro.ifAvailable(ob -> ob.send(mapper.toMessageAvro(delegateRecord, true)));
    }
}
