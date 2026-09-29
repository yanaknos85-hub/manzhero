package ru.sberbank.ditsib.corpclient.messaging.sender.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.messaging.senders.mappers.ContactMessageMapperImpl;
import ru.sber.transport.messages.corporate.avro.EmployeeMessage;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.utils.SimpleObjectProvider;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.corpclient.mapper.*;
import ru.sberbank.ditsib.corpclient.messaging.sender.EmployeeSender;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка отправителя сообщений сотрудников")
class EmployeeSenderImplTest {

    private final SimpleObjectProvider<OutputBridge> plaintextProvider = new SimpleObjectProvider<>(null);

    private final SimpleObjectProvider<OutputBridge> sslProvider = new SimpleObjectProvider<>(null);

    private final SimpleObjectProvider<OutputBridge> avroProvider = new SimpleObjectProvider<>(null);

    private final EmployeeSender sender = new EmployeeSenderImpl(plaintextProvider, sslProvider, avroProvider, new EmployeeMapperImpl(
            new PersonalCarMapperImpl(), new AttributeMapperImpl(), new DateMapperImpl(), new ActiveStatusMapperImpl(), new ContactMessageMapperImpl()));

    @Test
    @DisplayName("Отправка минимального сообщения")
    void test_send_minimum() {
        var bridge = mock(OutputBridge.class);

        avroProvider.set(bridge);

        var organization = new Organization();
        organization.setId(UUID.randomUUID());
        var department = new Department();
        department.setId(UUID.randomUUID());
        var position = new Position();
        position.setId(UUID.randomUUID());
        var source = new Employee();
        source.setId(UUID.randomUUID());
        source.setDepartment(department);
        source.setOrganization(organization);
        source.setPosition(position);
        source.setFirstName(Instancio.create(String.class));
        source.setPersonnelNumber(Instancio.create(String.class));
        source.setLastName(Instancio.create(String.class));
        source.setActiveStatus(Instancio.create(ActiveStatus.class));
        source.setHumanReadableId(Instancio.create(String.class));
        source.setConsent(Instancio.create(Boolean.class));

        sender.send(source);

        var messageCaptor = ArgumentCaptor.forClass(EmployeeMessage.class);

        verify(bridge).send(messageCaptor.capture());

        var value = messageCaptor.getValue();
        assertThat(value).isNotNull();
        assertSoftly(it -> {
            it.assertThat(value.getId()).isEqualTo(source.getId());
            it.assertThat(value.getDepartmentId()).isEqualTo(source.getDepartment().getId());
            it.assertThat(value.getPositionId()).isEqualTo(source.getPosition().getId());
            it.assertThat(value.getFirstName()).isEqualTo(source.getFirstName());
            it.assertThat(value.getDeleted()).isEqualTo(source.getActiveStatus() == ActiveStatus.INACTIVE);
            it.assertThat(value.getLastName()).isEqualTo(source.getLastName());
            it.assertThat(value.getHumanReadableId()).isEqualTo(source.getHumanReadableId());
            it.assertThat(value.getConsent()).isEqualTo(source.isConsent());
        });
    }

}