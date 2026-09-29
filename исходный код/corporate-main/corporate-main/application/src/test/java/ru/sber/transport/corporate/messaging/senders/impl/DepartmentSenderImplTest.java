package ru.sber.transport.corporate.messaging.senders.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.Active;
import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.business.model.Organization;
import ru.sber.transport.corporate.business.model.StructureType;
import ru.sber.transport.corporate.messaging.mappers.ActiveMessageMapperImpl;
import ru.sber.transport.corporate.messaging.senders.DepartmentSender;
import ru.sber.transport.corporate.messaging.senders.mappers.DepartmentMessageMapperImpl;
import ru.sber.transport.messages.corporate.avro.DepartmentMessage;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.utils.SimpleObjectProvider;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка сервиса отправки подразделений")
class DepartmentSenderImplTest {

    private final SimpleObjectProvider<OutputBridge> plaintextProvider = new SimpleObjectProvider<>(null);

    private final SimpleObjectProvider<OutputBridge> sslProvider = new SimpleObjectProvider<>(null);

    private final SimpleObjectProvider<OutputBridge> avroProvider = new SimpleObjectProvider<>(null);

    private final DepartmentSender sender = new DepartmentSenderImpl(plaintextProvider, sslProvider, avroProvider, new DepartmentMessageMapperImpl(new ActiveMessageMapperImpl()));

    @Test
    @DisplayName("Отправка минимального сообщения")
    void test_send_minimum() {
        var bridge = mock(OutputBridge.class);

        avroProvider.set(bridge);

        var organization = new Organization();
        organization.setId(UUID.randomUUID());
        var department = new Department(Instancio.create(String.class), Instancio.create(String.class), Instancio.create(Active.class), Instancio.create(StructureType.class));
        department.setId(UUID.randomUUID());
        department.setParentId(Instancio.create(UUID.class));
        department.setLevelCode(Instancio.create(String.class));
        department.setLevelName(Instancio.create(String.class));
        department.setHumanReadableId(Instancio.create(String.class));
        department.setSyncId(Instancio.create(String.class));
        department.setHandmade(Instancio.create(Boolean.class));
        department.setOrganizationId(organization.getId());

        sender.send(department);

        var messageCaptor = ArgumentCaptor.forClass(DepartmentMessage.class);

        verify(bridge).send(messageCaptor.capture(), eq(Map.of("syncId", department.getSyncId())));

        var value = messageCaptor.getValue();
        assertThat(value).isNotNull();
        assertSoftly(it -> {
            it.assertThat(value.getId()).isEqualTo(department.getId());
            it.assertThat(value.getParentId()).isEqualTo(department.getParentId());
            it.assertThat(value.getOrganizationId()).isEqualTo(department.getOrganizationId());
            it.assertThat(value.getCode()).isEqualTo(department.getCode());
            it.assertThat(value.getName()).isEqualTo(department.getName());
            it.assertThat(value.getEasupId()).isEqualTo(department.getSyncId());
            it.assertThat(value.getDeleted()).isEqualTo(department.getStatus() == Active.INACTIVE);
            it.assertThat(value.getHumanReadableId()).isEqualTo(department.getHumanReadableId());
        });
    }
}