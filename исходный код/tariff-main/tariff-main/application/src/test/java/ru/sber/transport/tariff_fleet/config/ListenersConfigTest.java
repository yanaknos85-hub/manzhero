package ru.sber.transport.tariff_fleet.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.transport.tariff_fleet.messaging.listener.ListenersConfig;
import ru.sber.transport.tariff_fleet.provider.DepartmentProvider;
import ru.sber.transport.tariff_fleet.provider.EmployeeProvider;
import ru.sber.transport.tariff_fleet.provider.OrganizationProvider;
import ru.sber.transport.tariff_fleet.provider.PositionProvider;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ListenersConfigTest {

    private final ListenersConfig config = new ListenersConfig();

    @Test
    void organizationsInputSave() {
        var provider = mock(OrganizationProvider.class);
        var message = new OrganizationMessage();
        message.setId(UUID.randomUUID());
        message.setOfficialName("officialName1");
        message.setTid("tid");
        message.setMsrn("msrn");
        message.setDeleted(false);
        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));
        var input = config.organizationsInput(provider);
        input.accept(rawMessage);
        var captor = ArgumentCaptor.forClass(OrganizationMessage.class);
        verify(provider).save(captor.capture());
        var actual = captor.getValue();
        assertEquals(message, actual);
    }

    @Test
    void organizationsInputDelete() {
        var provider = mock(OrganizationProvider.class);
        var message = new OrganizationMessage();
        message.setId(UUID.randomUUID());
        message.setOfficialName("officialName1");
        message.setTid("tid");
        message.setMsrn("msrn");
        message.setDeleted(true);
        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));
        var input = config.organizationsInput(provider);
        input.accept(rawMessage);
        var captor = ArgumentCaptor.forClass(OrganizationMessage.class);
        verify(provider).delete(captor.capture());
        var actual = captor.getValue();
        assertEquals(message, actual);
    }

    @Test
    void departmentsInputSave() {
        var provider = mock(DepartmentProvider.class);
        var message = DepartmentMessage.builder()
                .id(UUID.randomUUID())
                .departmentName("departmentName1")
                .organizationId(UUID.randomUUID())
                .location("location")
                .code("code")
                .humanReadableId("DT-0001-00000001")
                .deleted(false)
                .build();
        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));
        var input = config.departmentsInput(provider);
        input.accept(rawMessage);
        var captor = ArgumentCaptor.forClass(DepartmentMessage.class);
        verify(provider).save(captor.capture());
        var actual = captor.getValue();
        assertEquals(message, actual);
    }

    @Test
    void departmentsInputDelete() {
        var provider = mock(DepartmentProvider.class);
        var message = DepartmentMessage.builder()
                .id(UUID.randomUUID())
                .departmentName("departmentName1")
                .organizationId(UUID.randomUUID())
                .location("location")
                .code("code")
                .humanReadableId("DT-0001-00000001")
                .deleted(true)
                .build();
        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));
        var input = config.departmentsInput(provider);
        input.accept(rawMessage);
        var captor = ArgumentCaptor.forClass(DepartmentMessage.class);
        verify(provider).delete(captor.capture());
        var actual = captor.getValue();
        assertEquals(message, actual);
    }

    @Test
    void positionsInputSave() {
        var provider = mock(PositionProvider.class);
        var message = PositionMessage.builder()
                .id(UUID.randomUUID())
                .positionName("positionName1")
                .organizationId(UUID.randomUUID())
                .selfApproved(true)
                .deleted(false)
                .build();
        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));
        var input = config.positionsInput(provider);
        input.accept(rawMessage);
        var captor = ArgumentCaptor.forClass(PositionMessage.class);
        verify(provider).save(captor.capture());
        var actual = captor.getValue();
        assertEquals(message, actual);
    }

    @Test
    void positionsInputDelete() {
        var provider = mock(PositionProvider.class);
        var message = PositionMessage.builder()
                .id(UUID.randomUUID())
                .positionName("positionName1")
                .organizationId(UUID.randomUUID())
                .selfApproved(true)
                .deleted(true)
                .build();
        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));
        var input = config.positionsInput(provider);
        input.accept(rawMessage);
        var captor = ArgumentCaptor.forClass(PositionMessage.class);
        verify(provider).delete(captor.capture());
        var actual = captor.getValue();
        assertEquals(message, actual);
    }

    @Test
    void employeesInputSave() {
        var provider = mock(EmployeeProvider.class);
        var message = EmployeeMessage.builder()
                .id(UUID.randomUUID())
                .personnelNumber("0000001")
                .mobilePhone("+792356811")
                .patronymic("Александровна")
                .lastName("Гришина")
                .firstName("Светлана")
                .positionId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .departmentId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .humanReadableId("US-0001-00000001")
                .deleted(false)
                .build();
        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));
        var input = config.employeesInput(provider);
        input.accept(rawMessage);
        var captor = ArgumentCaptor.forClass(EmployeeMessage.class);
        verify(provider).save(captor.capture());
        var actual = captor.getValue();
        assertEquals(message, actual);
    }

    @Test
    void employeesInputDelete() {
        var provider = mock(EmployeeProvider.class);
        var message = EmployeeMessage.builder()
                .id(UUID.randomUUID())
                .personnelNumber("0000001")
                .mobilePhone("+792356811")
                .patronymic("Александровна")
                .lastName("Гришина")
                .firstName("Светлана")
                .positionId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .departmentId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .humanReadableId("US-0001-00000001")
                .deleted(true)
                .build();
        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));
        var input = config.employeesInput(provider);
        input.accept(rawMessage);
        var captor = ArgumentCaptor.forClass(EmployeeMessage.class);
        verify(provider).delete(captor.capture());
        var actual = captor.getValue();
        assertEquals(message, actual);
    }
}