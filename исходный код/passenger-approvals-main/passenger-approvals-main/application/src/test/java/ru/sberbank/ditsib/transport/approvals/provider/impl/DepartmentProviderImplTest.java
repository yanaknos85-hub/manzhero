package ru.sberbank.ditsib.transport.approvals.provider.impl;

import ch.qos.logback.classic.Level;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.approvals.LoggingExtension;
import ru.sberbank.ditsib.transport.approvals.database.model.Department;
import ru.sberbank.ditsib.transport.approvals.database.model.Organization;
import ru.sberbank.ditsib.transport.approvals.database.model.TripRequestApproval;
import ru.sberbank.ditsib.transport.approvals.mappers.DepartmentMapper;
import ru.sberbank.ditsib.transport.approvals.services.ApproveService;
import ru.sberbank.ditsib.transport.approvals.services.DepartmentService;
import ru.sberbank.ditsib.transport.approvals.services.OrganizationService;
import ru.sberbank.ditsib.transport.approvals.services.TripApproverService;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка провайдера подразделений")
class DepartmentProviderImplTest {

    @InjectMocks
    private DepartmentProviderImpl provider;
    @Mock
    private DepartmentService service;
    @Mock
    private DepartmentMapper mapper;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private ApproveService<TripRequestApproval> tripApproveService;
    @Mock
    private TripApproverService approverService;
    @Captor
    private ArgumentCaptor<Department> departmentArgumentCaptor;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(DepartmentProviderImpl.class);

    @Test
    void delete() {
        var message1 = Instancio.create(DepartmentMessage.class);
        var message2 = Instancio.create(DepartmentMessage.class);
        var entity = Instancio.create(Department.class);
        doReturn(Optional.of(entity)).when(service).get(message1.getId());
        doReturn(Optional.empty()).when(service).get(message2.getId());
        doNothing().when(service).delete(entity);
        provider.delete(message1);
        provider.delete(message2);
        verify(service, times(2)).get(any(UUID.class));
        verify(service).delete(any(Department.class));
    }

    @Test
    void save() {
        var message1 = Instancio.create(DepartmentMessage.class);
        var message2 = Instancio.create(DepartmentMessage.class);
        var message3 = Instancio.of(DepartmentMessage.class)
                .set(field(DepartmentMessage::getOrganizationId), null)
                .create();
        var entity1 = Instancio.create(Department.class);
        var entity2 = Instancio.create(Department.class);
        var organization = Instancio.create(Organization.class);
        doReturn(entity1).when(mapper).departmentMessageToDepartment(message1);
        doReturn(entity2).when(mapper).departmentMessageToDepartment(message2);
        doReturn(Optional.of(organization)).when(organizationService).get(message1.getOrganizationId());
        doReturn(Optional.empty()).when(organizationService).get(message2.getOrganizationId());
        doReturn(entity1).when(service).save(departmentArgumentCaptor.capture());
        doNothing().when(tripApproveService).handlingAutoApproving(entity1);
        doNothing().when(approverService).onDepartmentChanged(entity1.getId());
        doNothing().when(organizationService).saveGrpcEntity(
                "Can't save department id:%s, entityName:%s, organizationId:%s, awaiting organization synchronization".formatted(message2.getId(),
                        message2.getDepartmentName(),
                        message2.getOrganizationId()),
                message2.getOrganizationId());
        provider.save(message1);
        provider.save(message2);
        provider.save(message3);
        assertThat(departmentArgumentCaptor.getAllValues()).hasSize(2);
        assertThat(departmentArgumentCaptor.getAllValues().get(0))
                .usingRecursiveComparison()
                .isEqualTo(entity1);
        assertThat(departmentArgumentCaptor.getAllValues().get(1))
                .usingRecursiveComparison()
                .isEqualTo(entity2);
        verify(mapper, times(2)).departmentMessageToDepartment(any(DepartmentMessage.class));
        verify(service, times(2)).save(any(Department.class));
        verify(organizationService, times(2)).get(any(UUID.class));
        verify(organizationService).saveGrpcEntity(anyString(), any(UUID.class));
        verify(tripApproveService, times(2)).handlingAutoApproving(any(Department.class));
        verify(approverService, times(2)).onDepartmentChanged(any(UUID.class));
        assertEquals(1, LOGGING_EXTENSION.getEvents().size());
        var loggingEvent = LOGGING_EXTENSION.getEvents().get(0);
        assertThat(loggingEvent.getLoggerName()).isEqualTo(DepartmentProviderImpl.class.getName());
        assertThat(loggingEvent.getFormattedMessage())
                .isEqualTo("Can't save department id:%s, name:%s, organizationId:%s, organization isn't present".formatted(message3.getId(),
                        message3.getDepartmentName(),
                        message3.getOrganizationId()));
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.INFO);
    }
}