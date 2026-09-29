package ru.sber.transport.corporate.messaging.senders.impl;

import org.springframework.beans.factory.ObjectProvider;
import ru.sber.transport.corporate.messaging.senders.DelegateSender;
import ru.sber.transport.corporate.model.Delegate;
import ru.sber.transport.messages.corporate.avro.DelegateData;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

public class DelegateSenderImpl implements DelegateSender {

    private final ObjectProvider<OutputBridge> delegateSenderAvro;

    public DelegateSenderImpl(ObjectProvider<OutputBridge> delegateSenderAvro) {
        this.delegateSenderAvro = delegateSenderAvro;
    }

    @Override
    public void send(Delegate data) {
        delegateSenderAvro.ifAvailable(it -> it.send(toMessage(data)));
    }

    private DelegateData toMessage(Delegate data) {
        return DelegateData.newBuilder()
                .setId(data.id())
                .setSupervisorId(data.supervisorId())
                .setDelegateId(data.delegateId())
                .setStartDate(data.startDate())
                .setEndDate(data.endDate())
                .setTransportType(data.type())
                .setDeleted(data.deleted())
                .build();
    }
}
