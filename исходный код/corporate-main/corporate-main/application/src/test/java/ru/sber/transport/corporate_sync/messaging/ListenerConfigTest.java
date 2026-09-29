package ru.sber.transport.corporate_sync.messaging;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.integration.support.MessageBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate_sync.Sync;
import ru.sber.transport.messages.easup.avro.*;

import java.time.Instant;
import java.time.LocalDate;

import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка слушателя сообщений")
class ListenerConfigTest {

    private final ListenerConfig config = new ListenerConfig();

    @Test
    @DisplayName("Создание сообщения о новом подразделении")
    void test_newDepartment() {
        var departmentSync = mock(Sync.class);
        var positionSync = mock(Sync.class);
        var employeeSync = mock(Sync.class);
        var departmentHeadSync = mock(Sync.class);
        var departmentParentSync = mock(Sync.class);
        var endDataSync = mock(Sync.class);

        var message = DepartmentData.newBuilder().setId(Instancio.create(String.class)).setOrganizationId(Instancio.create(String.class)).setActive(Instancio.create(Boolean.class)).setCode(Instancio.create(String.class)).setName(Instancio.create(String.class)).setUpdateDate(Instant.now()).setStartDate(LocalDate.now()).build();

        var input = config.organizationsInputAvro(departmentSync, positionSync, employeeSync, departmentHeadSync, departmentParentSync, endDataSync);

        var rawMessage = MessageBuilder.withPayload(OrganizationData.newBuilder().setId(Instancio.create(String.class)).setData(message).build()).build();

        input.accept(rawMessage);

        verify(departmentSync).sync(message.getOrganizationId(), message.getId(), message);
        verify(positionSync, never()).sync(any(), any(), any());
        verify(employeeSync, never()).sync(any(), any(), any());
        verify(departmentHeadSync, never()).sync(any(), any(), any());
        verify(departmentParentSync, never()).sync(any(), any(), any());
        verify(endDataSync, never()).sync(any(), any(), any());
    }

    @Test
    @DisplayName("Создание сообщения о новой должности")
    void test_newPosition() {
        var departmentSync = mock(Sync.class);
        var positionSync = mock(Sync.class);
        var employeeSync = mock(Sync.class);
        var departmentHeadSync = mock(Sync.class);
        var departmentParentSync = mock(Sync.class);
        var endDataSync = mock(Sync.class);

        var message = PositionData.newBuilder().setId(Instancio.create(String.class)).setOrganizationId(Instancio.create(String.class)).setActive(Instancio.create(Boolean.class)).setChief(Instancio.create(Boolean.class)).setName(Instancio.create(String.class)).setCode(Instancio.create(String.class)).setUpdateDate(Instant.now()).setStartDate(LocalDate.now()).build();

        var input = config.organizationsInputAvro(departmentSync, positionSync, employeeSync, departmentHeadSync, departmentParentSync, endDataSync);

        var rawMessage = MessageBuilder.withPayload(OrganizationData.newBuilder().setId(Instancio.create(String.class)).setData(message).build()).build();

        input.accept(rawMessage);

        verify(positionSync).sync(message.getOrganizationId(), message.getId(), message);
        verify(departmentSync, never()).sync(any(), any(), any());
        verify(employeeSync, never()).sync(any(), any(), any());
        verify(departmentHeadSync, never()).sync(any(), any(), any());
        verify(departmentParentSync, never()).sync(any(), any(), any());
        verify(endDataSync, never()).sync(any(), any(), any());
    }

    @Test
    @DisplayName("Создание сообщения о новом сотруднике")
    void test_newEmployee() {
        var departmentSync = mock(Sync.class);
        var positionSync = mock(Sync.class);
        var employeeSync = mock(Sync.class);
        var departmentHeadSync = mock(Sync.class);
        var departmentParentSync = mock(Sync.class);
        var endDataSync = mock(Sync.class);

        var message = EmployeeData.newBuilder().setPersonnelNumber(Instancio.create(String.class)).setActive(Instancio.create(Boolean.class)).setOrganizationId(Instancio.create(String.class)).setItinerantType(Instancio.create(ItinerantType.class)).setConsent(Instancio.create(Boolean.class)).setGender(Instancio.create(Gender.class)).setFirstName(Instancio.create(String.class)).setLastName(Instancio.create(String.class)).setDepartmentId(Instancio.create(String.class)).setPositionId(Instancio.create(String.class)).setUpdateDate(Instant.now()).setHireDate(LocalDate.now()).build();

        var input = config.organizationsInputAvro(departmentSync, positionSync, employeeSync, departmentHeadSync, departmentParentSync, endDataSync);

        var rawMessage = MessageBuilder.withPayload(OrganizationData.newBuilder().setId(Instancio.create(String.class)).setData(message).build()).build();

        input.accept(rawMessage);

        verify(employeeSync).sync(message.getOrganizationId(), message.getPersonnelNumber(), message);
        verify(departmentSync, never()).sync(any(), any(), any());
        verify(positionSync, never()).sync(any(), any(), any());
        verify(departmentHeadSync, never()).sync(any(), any(), any());
        verify(departmentParentSync, never()).sync(any(), any(), any());
        verify(endDataSync, never()).sync(any(), any(), any());
    }

    @Test
    @DisplayName("Создание сообщения о руководителе")
    void test_newDepartmentHead() {
        var departmentSync = mock(Sync.class);
        var positionSync = mock(Sync.class);
        var employeeSync = mock(Sync.class);
        var departmentHeadSync = mock(Sync.class);
        var departmentParentSync = mock(Sync.class);
        var endDataSync = mock(Sync.class);

        var message = DepartmentHeadData.newBuilder().setId(Instancio.create(String.class)).setOrganizationId(Instancio.create(String.class)).setEmployeeId(Instancio.create(String.class)).build();

        var input = config.organizationsInputAvro(departmentSync, positionSync, employeeSync, departmentHeadSync, departmentParentSync, endDataSync);

        var rawMessage = MessageBuilder.withPayload(OrganizationData.newBuilder().setId(Instancio.create(String.class)).setData(message).build()).build();

        input.accept(rawMessage);

        verify(departmentHeadSync).sync(message.getOrganizationId(), message.getId(), message);
        verify(departmentSync, never()).sync(any(), any(), any());
        verify(positionSync, never()).sync(any(), any(), any());
        verify(employeeSync, never()).sync(any(), any(), any());
        verify(departmentParentSync, never()).sync(any(), any(), any());
        verify(endDataSync, never()).sync(any(), any(), any());
    }

    @Test
    @DisplayName("Создание сообщения о родителе")
    void test_newDepartmentParent() {
        var departmentSync = mock(Sync.class);
        var positionSync = mock(Sync.class);
        var employeeSync = mock(Sync.class);
        var departmentHeadSync = mock(Sync.class);
        var departmentParentSync = mock(Sync.class);
        var endDataSync = mock(Sync.class);

        var message = DepartmentParentData.newBuilder().setId(Instancio.create(String.class)).setOrganizationId(Instancio.create(String.class)).setParentId(Instancio.create(String.class)).build();

        var input = config.organizationsInputAvro(departmentSync, positionSync, employeeSync, departmentHeadSync, departmentParentSync, endDataSync);

        var rawMessage = MessageBuilder.withPayload(OrganizationData.newBuilder().setId(Instancio.create(String.class)).setData(message).build()).build();

        input.accept(rawMessage);

        verify(departmentParentSync).sync(message.getOrganizationId(), message.getId(), message);
        verify(departmentSync, never()).sync(any(), any(), any());
        verify(positionSync, never()).sync(any(), any(), any());
        verify(employeeSync, never()).sync(any(), any(), any());
        verify(departmentHeadSync, never()).sync(any(), any(), any());
        verify(endDataSync, never()).sync(any(), any(), any());
    }

    @Test
    @DisplayName("Создание сообщения об окончании")
    void test_newEnd() {
        var departmentSync = mock(Sync.class);
        var positionSync = mock(Sync.class);
        var employeeSync = mock(Sync.class);
        var departmentHeadSync = mock(Sync.class);
        var departmentParentSync = mock(Sync.class);
        var endDataSync = mock(Sync.class);

        var message = EndData.newBuilder().setEnd(true).build();

        var input = config.organizationsInputAvro(departmentSync, positionSync, employeeSync, departmentHeadSync, departmentParentSync, endDataSync);

        String id = Instancio.create(String.class);
        var rawMessage = MessageBuilder.withPayload(OrganizationData.newBuilder().setId(id).setData(message).build()).build();

        input.accept(rawMessage);

        verify(endDataSync).sync(id, id, message);
        verify(departmentSync, never()).sync(any(), any(), any());
        verify(positionSync, never()).sync(any(), any(), any());
        verify(employeeSync, never()).sync(any(), any(), any());
        verify(departmentHeadSync, never()).sync(any(), any(), any());
        verify(departmentParentSync, never()).sync(any(), any(), any());
    }

}