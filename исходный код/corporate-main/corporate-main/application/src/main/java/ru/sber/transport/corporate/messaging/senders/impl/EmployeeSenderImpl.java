package ru.sber.transport.corporate.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.messaging.senders.EmployeeSender;
import ru.sber.transport.corporate.messaging.senders.mappers.EmployeesMessageMapper;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.corpclient.messaging.sender.UserSender;

import java.util.HashMap;

@RequiredArgsConstructor
@Component
class EmployeeSenderImpl implements EmployeeSender {

    @SuppressWarnings("Autowired")
    @Qualifier("employeesOutput")
    private final ObjectProvider<OutputBridge> employeesOutputBridge;

    @SuppressWarnings("Autowired")
    @Qualifier("employeesOutputSsl")
    private final ObjectProvider<OutputBridge> employeesOutputSslBridge;

    @SuppressWarnings("Autowired")
    @Qualifier("employeesOutputAvro")
    private final ObjectProvider<OutputBridge> employeesOutputAvroBridge;

    private final EmployeesMessageMapper mapper;

    private final UserSender userSender;

    @Override
    public void send(Employee source) {
        var headers = new HashMap<String, Object>();
        var syncId = source.getSyncId();
        if (syncId != null) {
            headers.put("syncId", syncId);
        }
        var message = mapper.toMessage(source);
        employeesOutputBridge.ifAvailable(ob -> ob.send(message, headers));
        employeesOutputSslBridge.ifAvailable(ob -> ob.send(message, headers));
        employeesOutputAvroBridge.ifAvailable(ob -> ob.send(mapper.toMessageAvro(source), headers));
        userSender.send(source, null);
    }
}
