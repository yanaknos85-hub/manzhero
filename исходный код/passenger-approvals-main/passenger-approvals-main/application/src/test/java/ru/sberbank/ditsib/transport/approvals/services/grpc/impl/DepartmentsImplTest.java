package ru.sberbank.ditsib.transport.approvals.services.grpc.impl;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.commons.JUnitException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.corporate.grpc.service.DepartmentsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sberbank.ditsib.transport.approvals.LoggingExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class DepartmentsImplTest {
    
    @InjectMocks
    private DepartmentsImpl departments;
    @Mock
    private DepartmentsGrpc.DepartmentsBlockingStub stub;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(DepartmentsImpl.class);
    
    @Test
    void one() {
        var id = UUID.randomUUID();
        var response = OrganizationsOuterClass.Department.newBuilder()
                                                         .setId(id.toString())
                                                         .setHumanReadableId(UUID.randomUUID().toString())
                                                         .setOrganizationId(UUID.randomUUID().toString())
                                                         .setCode(UUID.randomUUID().toString())
                                                         .setName(UUID.randomUUID().toString())
                                                         .setLocation(OrganizationsOuterClass.NullableString.newBuilder()
                                                                                                            .setValue(UUID.randomUUID().toString())
                                                                                                            .build())
                                                         .setDeleted(false)
                                                         .setParentId(OrganizationsOuterClass.NullableString.newBuilder()
                                                                                                            .setValue(UUID.randomUUID().toString())
                                                                                                            .build())
                                                         .setHeadId(OrganizationsOuterClass.NullableString.newBuilder()
                                                                                                          .setValue(UUID.randomUUID().toString())
                                                                                                          .build())
                                                         .build();
        doReturn(response).when(stub).one(any());
        var actual = departments.one(id);
        assertThat(actual.getId()).isEqualTo(UUID.fromString(response.getId()));
        assertThat(actual.getOrganizationId()).isEqualTo(UUID.fromString(response.getOrganizationId()));
        assertThat(actual.getParentId()).isEqualTo(UUID.fromString(response.getParentId().getValue()));
        assertThat(actual.getDepartmentHeadId()).isEqualTo(UUID.fromString(response.getHeadId().getValue()));
        assertThat(actual.getApprovers()).isEmpty();
        assertThat(actual.getDepartmentName()).isEqualTo(response.getName());
        assertThat(actual.isActive()).isEqualTo(!response.getDeleted());
        assertThat(LOGGING_EXTENSION.getEvents()).hasSize(2);
        var loggingEvent1 = LOGGING_EXTENSION.getEvents().get(0);
        assertThat(loggingEvent1.getLoggerName()).isEqualTo(DepartmentsImpl.class.getName());
        assertThat(loggingEvent1.getFormattedMessage())
                .isEqualTo("Department %s not found. Requesting from source", id);
        assertThat(loggingEvent1.getLevel()).isEqualTo(Level.INFO);
        var loggingEvent2 = LOGGING_EXTENSION.getEvents().get(1);
        assertThat(loggingEvent2.getLoggerName()).isEqualTo(DepartmentsImpl.class.getName());
        assertThat(loggingEvent2.getFormattedMessage())
                .isEqualTo("Department %s responded", id);
        assertThat(loggingEvent2.getLevel()).isEqualTo(Level.INFO);
    }

    @Test
    void oneNullFields() {
        var id = UUID.randomUUID();
        var response = OrganizationsOuterClass.Department.newBuilder()
                .setId(id.toString())
                .setHumanReadableId(UUID.randomUUID().toString())
                .setOrganizationId(UUID.randomUUID().toString())
                .setCode(UUID.randomUUID().toString())
                .setName(UUID.randomUUID().toString())
                .setLocation(OrganizationsOuterClass.NullableString.newBuilder()
                        .setValue(UUID.randomUUID().toString())
                        .build())
                .setDeleted(false)
                .setParentId(OrganizationsOuterClass.NullableString.newBuilder()
                        .build())
                .setHeadId(OrganizationsOuterClass.NullableString.newBuilder()
                        .build())
                .build();
        doReturn(response).when(stub).one(any());
        var actual = departments.one(id);
        assertThat(actual.getId()).isEqualTo(UUID.fromString(response.getId()));
        assertThat(actual.getOrganizationId()).isEqualTo(UUID.fromString(response.getOrganizationId()));
        assertThat(actual.getParentId()).isNull();
        assertThat(actual.getDepartmentHeadId()).isNull();
        assertThat(actual.getApprovers()).isEmpty();
        assertThat(actual.getDepartmentName()).isEqualTo(response.getName());
        assertThat(actual.isActive()).isEqualTo(!response.getDeleted());
        assertThat(LOGGING_EXTENSION.getEvents()).hasSize(2);
        var loggingEvent1 = LOGGING_EXTENSION.getEvents().get(0);
        assertThat(loggingEvent1.getLoggerName()).isEqualTo(DepartmentsImpl.class.getName());
        assertThat(loggingEvent1.getFormattedMessage())
                .isEqualTo("Department %s not found. Requesting from source", id);
        assertThat(loggingEvent1.getLevel()).isEqualTo(Level.INFO);
        var loggingEvent2 = LOGGING_EXTENSION.getEvents().get(1);
        assertThat(loggingEvent2.getLoggerName()).isEqualTo(DepartmentsImpl.class.getName());
        assertThat(loggingEvent2.getFormattedMessage())
                .isEqualTo("Department %s responded", id);
        assertThat(loggingEvent2.getLevel()).isEqualTo(Level.INFO);
    }
    
    @Test
    void oneWithException() {
        var id = UUID.randomUUID();
        doThrow(JUnitException.class).when(stub).one(any());
        departments.one(id);
        assertThat(LOGGING_EXTENSION.getEvents()).hasSize(3);
        var loggingEvent1 = LOGGING_EXTENSION.getEvents().get(0);
        assertThat(loggingEvent1.getLoggerName()).isEqualTo(DepartmentsImpl.class.getName());
        assertThat(loggingEvent1.getFormattedMessage())
                .isEqualTo("Department %s not found. Requesting from source", id);
        assertThat(loggingEvent1.getLevel()).isEqualTo(Level.INFO);
        var loggingEvent2 = LOGGING_EXTENSION.getEvents().get(1);
        assertThat(loggingEvent2.getLoggerName()).isEqualTo(DepartmentsImpl.class.getName());
        assertThat(loggingEvent2.getFormattedMessage())
                .isNull();
        assertThat(loggingEvent2.getLevel()).isEqualTo(Level.DEBUG);
        var loggingEvent3 = LOGGING_EXTENSION.getEvents().get(2);
        assertThat(loggingEvent3.getLoggerName()).isEqualTo(DepartmentsImpl.class.getName());
        assertThat(loggingEvent3.getFormattedMessage())
                .isEqualTo("Failed to receive department, id: %s, cause: null", id);
        assertThat(loggingEvent3.getLevel()).isEqualTo(Level.INFO);
    }
}