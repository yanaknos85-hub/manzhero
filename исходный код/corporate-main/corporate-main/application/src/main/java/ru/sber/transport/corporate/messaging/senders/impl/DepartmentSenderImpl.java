package ru.sber.transport.corporate.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.messaging.senders.DepartmentSender;
import ru.sber.transport.corporate.messaging.senders.mappers.DepartmentMessageMapper;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
class DepartmentSenderImpl implements DepartmentSender {

    @SuppressWarnings("Autowired")
    @Qualifier("departmentsOutput")
    private final ObjectProvider<OutputBridge> departmentsOutputBridge;

    @SuppressWarnings("Autowired")
    @Qualifier("departmentsOutputSsl")
    private final ObjectProvider<OutputBridge> departmentsOutputBridgeSsl;

    @SuppressWarnings("Autowired")
    @Qualifier("departmentsOutputAvro")
    private final ObjectProvider<OutputBridge> departmentsOutputBridgeAvro;

    private final DepartmentMessageMapper mapper;

    @Override
    public void send(Department source) {
        var headers = new HashMap<String, Object>();
        var syncId = source.getSyncId();
        if (syncId != null) {
            headers.put("syncId", syncId);
        }
        var message = mapper.toMessage(source);
        departmentsOutputBridge.ifAvailable(ob -> ob.send(message, headers));
        departmentsOutputBridgeSsl.ifAvailable(ob -> ob.send(message, headers));
        departmentsOutputBridgeAvro.ifAvailable(ob -> ob.send(mapper.toMessageAvro(source), headers));
    }
}
