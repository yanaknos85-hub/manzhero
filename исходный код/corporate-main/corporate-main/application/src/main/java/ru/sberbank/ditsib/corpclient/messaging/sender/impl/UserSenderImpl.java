package ru.sberbank.ditsib.corpclient.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.mapper.UserMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.UserSender;
import ru.sberbank.ditsib.transport.messaging.messages.UserMessage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of sender user.
 */
@Slf4j
@Service
@RequiredArgsConstructor
class UserSenderImpl implements UserSender {

    @Qualifier("usersOutput")
    private final ObjectProvider<OutputBridge> usersOutput;

    @Qualifier("usersOutputSsl")
    private final ObjectProvider<OutputBridge> usersOutputSsl;

    private final UserMapper userMapper;

    @Override
    public void send(Employee employee, Collection<String> roles) {
        var message = userMapper.toMessage(employee, roles);
        var headers = new HashMap<String, Object>(Map.of(UserMessage.TYPE, employee.getOrgStructureType().name()));
        Optional.ofNullable(usersOutput.getIfAvailable()).ifPresent(ob -> ob.send(message, headers));
        Optional.ofNullable(usersOutputSsl.getIfAvailable()).ifPresent(ob -> ob.send(message, headers));
    }

    @Override
    public void send(ru.sber.transport.corporate.business.model.Employee source, Collection<String> roles) {
        var message = userMapper.toMessage(source, roles);
        var headers = new HashMap<String, Object>(Map.of(UserMessage.TYPE, source.getType().name()));
        Optional.ofNullable(usersOutput.getIfAvailable()).ifPresent(ob -> ob.send(message, headers));
        Optional.ofNullable(usersOutputSsl.getIfAvailable()).ifPresent(ob -> ob.send(message, headers));
    }
}
