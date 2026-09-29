package ru.sber.transport.corporate.messaging.senders.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.Active;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.messaging.mappers.*;
import ru.sber.transport.corporate.messaging.senders.EmployeeSender;
import ru.sber.transport.corporate.messaging.senders.mappers.ContactMessageMapperImpl;
import ru.sber.transport.corporate.messaging.senders.mappers.EmployeesMessageMapperImpl;
import ru.sber.transport.messages.corporate.avro.EmployeeMessage;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.utils.SimpleObjectProvider;
import ru.sberbank.ditsib.corpclient.messaging.sender.UserSender;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка сервиса отправки подразделений")
class EmployeeSenderImplTest {

    private final SimpleObjectProvider<OutputBridge> plaintextProvider = new SimpleObjectProvider<>(null);

    private final SimpleObjectProvider<OutputBridge> sslProvider = new SimpleObjectProvider<>(null);

    private final SimpleObjectProvider<OutputBridge> avroProvider = new SimpleObjectProvider<>(null);

    private final UserSender userSender = mock(UserSender.class);

    private final EmployeeSender sender = new EmployeeSenderImpl(plaintextProvider, sslProvider, avroProvider, new EmployeesMessageMapperImpl(new ActiveMessageMapperImpl(), new ContactMessageMapperImpl(), new AttributeMessageMapperImpl()), userSender);

    @Test
    @DisplayName("Отправка минимального сообщения")
    void test_send_minimum() {
        var bridge = mock(OutputBridge.class);

        avroProvider.set(bridge);

        var employee = Instancio.create(Employee.class);

        sender.send(employee);

        var messageCaptor = ArgumentCaptor.forClass(EmployeeMessage.class);

        verify(bridge).send(messageCaptor.capture(), eq(Map.of("syncId", employee.getPersonnelNumber())));

        var value = messageCaptor.getValue();
        assertThat(value).isNotNull();
        assertSoftly(it -> {
            it.assertThat(value.getId()).isEqualTo(employee.getId());
            it.assertThat(value.getLastName()).isEqualTo(employee.getLastName());
            it.assertThat(value.getOrganizationId()).isEqualTo(employee.getOrganizationId());
            it.assertThat(value.getFirstName()).isEqualTo(employee.getFirstName());
            it.assertThat(value.getPatronymic()).isEqualTo(employee.getPatronymic());
            it.assertThat(value.getPersonnelNumber()).isEqualTo(employee.getPersonnelNumber());
            it.assertThat(value.getDeleted()).isEqualTo(employee.getStatus() == Active.INACTIVE);
            it.assertThat(value.getHumanReadableId()).isEqualTo(employee.getHumanReadableId());
        });
    }
}