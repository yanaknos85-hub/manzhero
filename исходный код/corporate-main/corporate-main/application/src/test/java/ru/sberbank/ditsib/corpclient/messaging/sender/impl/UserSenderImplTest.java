package ru.sberbank.ditsib.corpclient.messaging.sender.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.transport.corporate.business.model.StructureType;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.OrgStructureType;
import ru.sberbank.ditsib.corpclient.mapper.UserMapper;
import ru.sberbank.ditsib.transport.messaging.messages.UserMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка отправителя сообщений пользователей")
class UserSenderImplTest {
    @Mock
    private ObjectProvider<OutputBridge> usersOutput;
    @Mock
    private ObjectProvider<OutputBridge> usersOutputSsl;
    @Mock
    private OutputBridge outputBridge;
    @Mock
    private OutputBridge outputBridgeSsl;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserSenderImpl userSender;

    @BeforeEach
    void setup() {
        userSender = new UserSenderImpl(usersOutput, usersOutputSsl, userMapper);
    }

    @Test
    @DisplayName("Отправка сообщения для ru.sberbank.ditsib.corpclient.database.model.Employee")
    void testSendDbEmployee() {
        var roles = List.of("ROLE_USER");
        var employeeExternal = Instancio.of(Employee.class)
                .set(field(Employee::getOrgStructureType), OrgStructureType.EXTERNAL)
                .create();
        var employeeInternal = Instancio.of(Employee.class)
                .set(field(Employee::getOrgStructureType), OrgStructureType.INTERNAL)
                .create();
        var messageExternal = Instancio.create(UserMessage.class);
        var messageInternal = Instancio.create(UserMessage.class);

        doReturn(messageExternal).when(userMapper).toMessage(employeeExternal, roles);
        doReturn(messageInternal).when(userMapper).toMessage(employeeInternal, roles);
        doReturn(outputBridge).when(usersOutput).getIfAvailable();
        doReturn(outputBridgeSsl).when(usersOutputSsl).getIfAvailable();

        userSender.send(employeeExternal, roles);
        userSender.send(employeeInternal, roles);

        verify(userMapper, times(2)).toMessage(any(Employee.class), anyList());
        verify(usersOutput, times(2)).getIfAvailable();
        verify(usersOutputSsl, times(2)).getIfAvailable();
        verify(outputBridge, times(2)).send(any(UserMessage.class), anyMap());
        verify(outputBridgeSsl, times(2)).send(any(UserMessage.class), anyMap());
    }

    @Test
    @DisplayName("Отправка сообщения для ru.sber.transport.corporate.business.model.Employee")
    void testSendBusinessEmployee() {
        var roles = List.of("ROLE_USER");
        var employeeExternal = Instancio.of(ru.sber.transport.corporate.business.model.Employee.class)
                .set(field(ru.sber.transport.corporate.business.model.Employee::getType), StructureType.EXTERNAL)
                .create();
        var employeeInternal = Instancio.of(ru.sber.transport.corporate.business.model.Employee.class)
                .set(field(ru.sber.transport.corporate.business.model.Employee::getType), StructureType.INTERNAL)
                .create();
        var messageExternal = Instancio.create(UserMessage.class);
        var messageInternal = Instancio.create(UserMessage.class);
        var headers = new HashMap<String, Object>(Map.of(UserMessage.TYPE, StructureType.INTERNAL.name()));

        doReturn(messageExternal).when(userMapper).toMessage(employeeExternal, roles);
        doReturn(messageInternal).when(userMapper).toMessage(employeeInternal, roles);
        doReturn(outputBridge).when(usersOutput).getIfAvailable();
        doReturn(outputBridgeSsl).when(usersOutputSsl).getIfAvailable();

        userSender.send(employeeExternal, roles);
        userSender.send(employeeInternal, roles);

        verify(userMapper, times(2)).toMessage(any(ru.sber.transport.corporate.business.model.Employee.class), anyList());
        verify(usersOutput, times(2)).getIfAvailable();
        verify(usersOutputSsl, times(2)).getIfAvailable();
        verify(outputBridge, times(2)).send(any(UserMessage.class), anyMap());
        verify(outputBridgeSsl, times(2)).send(any(UserMessage.class), anyMap());
    }
}