package ru.sberbank.ditsib.corpclient.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.corpclient.database.model.Attribute;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.mapper.EmployeeMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.EmployeeSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация отправителя сотрудников.
 */
@Slf4j
@RequiredArgsConstructor
@Component("oldEmployeeSenderImpl")
@Transactional
class EmployeeSenderImpl implements EmployeeSender {

    @Qualifier("employeesOutput")
    private final ObjectProvider<OutputBridge> employeesOutput;

    @Qualifier("employeesOutputSsl")
    private final ObjectProvider<OutputBridge> employeesOutputSsl;

    @Qualifier("employeesOutputAvro")
    private final ObjectProvider<OutputBridge> employeesOutputAvro;

    private final EmployeeMapper mapper;

    @Override
    public void send(Collection<Employee> employee) {
        for (var value : employee) {
            var message = mapper.toMessage(value, value.getOrganization(), Optional.ofNullable(value.getAttributes()).orElseGet(Set::of).stream().map(Attribute::getName).collect(Collectors.toUnmodifiableSet()));
            employeesOutput.ifAvailable(ob -> ob.send(message));
            employeesOutputSsl.ifAvailable(ob -> ob.send(message));
            employeesOutputAvro.ifAvailable(ob -> ob.send(mapper.toMessageAvro(value)));
        }
    }
}
