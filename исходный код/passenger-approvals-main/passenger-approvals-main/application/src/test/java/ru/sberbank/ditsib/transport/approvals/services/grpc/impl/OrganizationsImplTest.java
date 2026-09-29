package ru.sberbank.ditsib.transport.approvals.services.grpc.impl;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.commons.JUnitException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.corporate.grpc.service.OrganizationsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sberbank.ditsib.transport.approvals.LoggingExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class OrganizationsImplTest {
    
    @InjectMocks
    private OrganizationsImpl organizations;
    @Mock
    private OrganizationsGrpc.OrganizationsBlockingStub stub;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(OrganizationsImpl.class);
    
    @Test
    void one() {
        var id = UUID.randomUUID();
        var response = OrganizationsOuterClass.Organization.newBuilder()
                                                           .setId(id.toString())
                                                           .setDigitId(1)
                                                           .setName(UUID.randomUUID().toString())
                                                           .setAddress(UUID.randomUUID().toString())
                                                           .setMsrn(UUID.randomUUID().toString())
                                                           .setTid(UUID.randomUUID().toString())
                                                           .setCode(OrganizationsOuterClass.NullableInt.newBuilder()
                                                                                                       .setValue(1)
                                                                                                       .build())
                                                           .setDeleted(false)
                                                           .setGroup(OrganizationsOuterClass.NullableString.newBuilder()
                                                                                                           .setValue(UUID.randomUUID().toString())
                                                                                                           .build())
                                                           .setType(OrganizationsOuterClass.StructureType.INTERNAL)
                                                           .build();
        doReturn(response).when(stub).one(any());
        var actual = organizations.one(id);
        assertThat(actual.getId()).isEqualTo(UUID.fromString(response.getId()));
        assertThat(actual.getDigitId()).isEqualTo(response.getDigitId());
        assertThat(actual.isActive()).isEqualTo(!response.getDeleted());
        assertThat(LOGGING_EXTENSION.getEvents()).hasSize(2);
        var loggingEvent1 = LOGGING_EXTENSION.getEvents().get(0);
        assertThat(loggingEvent1.getLoggerName()).isEqualTo(OrganizationsImpl.class.getName());
        assertThat(loggingEvent1.getFormattedMessage())
                .isEqualTo("Organization %s not found. Requesting from source", id);
        assertThat(loggingEvent1.getLevel()).isEqualTo(Level.INFO);
        var loggingEvent2 = LOGGING_EXTENSION.getEvents().get(1);
        assertThat(loggingEvent2.getLoggerName()).isEqualTo(OrganizationsImpl.class.getName());
        assertThat(loggingEvent2.getFormattedMessage())
                .isEqualTo("Organization %s responded", id);
        assertThat(loggingEvent2.getLevel()).isEqualTo(Level.INFO);
    }
    
    @Test
    void oneWithException() {
        var id = UUID.randomUUID();
        doThrow(JUnitException.class).when(stub).one(any());
        organizations.one(id);
        assertThat(LOGGING_EXTENSION.getEvents()).hasSize(3);
        var loggingEvent1 = LOGGING_EXTENSION.getEvents().get(0);
        assertThat(loggingEvent1.getLoggerName()).isEqualTo(OrganizationsImpl.class.getName());
        assertThat(loggingEvent1.getFormattedMessage())
                .isEqualTo("Organization %s not found. Requesting from source", id);
        assertThat(loggingEvent1.getLevel()).isEqualTo(Level.INFO);
        var loggingEvent2 = LOGGING_EXTENSION.getEvents().get(1);
        assertThat(loggingEvent2.getLoggerName()).isEqualTo(OrganizationsImpl.class.getName());
        assertThat(loggingEvent2.getFormattedMessage())
                .isNull();
        assertThat(loggingEvent2.getLevel()).isEqualTo(Level.DEBUG);
        var loggingEvent3 = LOGGING_EXTENSION.getEvents().get(2);
        assertThat(loggingEvent3.getLoggerName()).isEqualTo(OrganizationsImpl.class.getName());
        assertThat(loggingEvent3.getFormattedMessage())
                .isEqualTo("Failed to receive organization, id: %s, cause: null", id);
        assertThat(loggingEvent3.getLevel()).isEqualTo(Level.INFO);
    }
}