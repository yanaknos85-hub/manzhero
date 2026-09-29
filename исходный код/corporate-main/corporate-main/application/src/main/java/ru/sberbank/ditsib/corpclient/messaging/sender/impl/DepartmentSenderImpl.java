package ru.sberbank.ditsib.corpclient.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.mapper.DepartmentMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.DepartmentSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

import java.util.Collection;

/**
 * Implementation of department sender.
 */
@Slf4j
@RequiredArgsConstructor
@Component("oldDepartmentSenderImpl")
class DepartmentSenderImpl implements DepartmentSender {

    @Qualifier("departmentsOutput")
    private final ObjectProvider<OutputBridge> departmentsOutput;

    @Qualifier("departmentsOutputSsl")
    private final ObjectProvider<OutputBridge> departmentsOutputSsl;

    @Qualifier("departmentsOutputAvro")
    private final ObjectProvider<OutputBridge> departmentsOutputAvro;
    
    private final DepartmentMapper mapper;

    @Override
    public void send(Collection<Department> department) {
        for (var value : department) {
            var message = mapper.toMessage(value);
            departmentsOutput.ifAvailable(ob -> ob.send(message));
            departmentsOutputSsl.ifAvailable(ob -> ob.send(message));
            departmentsOutputAvro.ifAvailable(ob -> ob.send(mapper.toAvroMessage(value)));
        }
    }
}
