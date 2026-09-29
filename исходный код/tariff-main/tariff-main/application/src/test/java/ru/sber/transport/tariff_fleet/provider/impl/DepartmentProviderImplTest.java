package ru.sber.transport.tariff_fleet.provider.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.database.model.Department;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.exception.AwaitingSynchronizationException;
import ru.sber.transport.tariff_fleet.mapper.DepartmentMapper;
import ru.sber.transport.tariff_fleet.service.DepartmentService;
import ru.sber.transport.tariff_fleet.service.OrganizationService;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static ru.sber.transport.tariff_fleet.provider.impl.DepartmentProviderImpl.ERROR_NOT_IN_DB_MESSAGE_FORMAT;

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
    private Organization organization;
    private Department department;
    private DepartmentMessage message;

    @BeforeEach
    void setUp() {
        organization = Organization.builder()
                                   .id(UUID.randomUUID())
                                   .digitId(1L)
                                   .officialName("officialName")
                                   .build();
        department = Department.builder()
                               .id(UUID.randomUUID())
                               .organizationId(organization.getId())
                               .departmentName("departmentName")
                               .humanReadableId("humanReadableId")
                               .build();
        message = DepartmentMessage.builder()
                                   .id(department.getId())
                                   .departmentName(department.getDepartmentName())
                                   .organizationId(organization.getId())
                                   .location("location")
                                   .code("code")
                                   .humanReadableId(department.getHumanReadableId())
                                   .build();
    }

    @Test
    void delete() {
        var notExistId = UUID.randomUUID();
        var message2 = DepartmentMessage.builder()
                                        .id(notExistId)
                                        .departmentName("departmentName2")
                                        .organizationId(organization.getId())
                                        .location("location2")
                                        .code("code2")
                                        .humanReadableId("humanReadableId")
                                        .build();
        when(service.get(message.getId())).thenReturn(Optional.of(department));
        when(service.get(notExistId)).thenReturn(Optional.empty());
        doNothing().when(service).delete(department);
        provider.delete(message);
        provider.delete(message2);
        verify(service).get(message.getId());
        verify(service).get(message2.getId());
        verify(service).delete(department);
        verify(service).delete(any());
    }

    @Test
    void saveOrUpdate() {
        when(mapper.departmentMessageToDepartment(message)).thenReturn(department);
        when(organizationService.get(message.getOrganizationId())).thenReturn(Optional.of(organization));
        doNothing().when(service).saveOrUpdate(department);
        provider.save(message);
        verify(mapper).departmentMessageToDepartment(message);
        verify(service).saveOrUpdate(department);
        verify(organizationService).get(message.getOrganizationId());
        verify(service, never()).get(message.getParentId());
    }

    @Test
    void saveNoOrganizationInDatabase() {
        when(organizationService.get(message.getOrganizationId())).thenReturn(Optional.empty());
        var exception = assertThrows(AwaitingSynchronizationException.class, () -> provider
                .save(message));
        assertEquals(String.format(ERROR_NOT_IN_DB_MESSAGE_FORMAT,
                                   message.getId(),
                                   message.getDepartmentName(),
                                   message.getOrganizationId(),
                                   "organization"), exception.getMessage());
        verify(mapper, never()).departmentMessageToDepartment(message);
        verify(service, never()).saveOrUpdate(department);
        verify(organizationService).get(message.getOrganizationId());
        verify(service, never()).get(message.getParentId());
    }

    @Test
    void saveNoParentInDatabase() {
        var messageNoParent = DepartmentMessage.builder()
                                               .id(department.getId())
                                               .departmentName(department.getDepartmentName())
                                               .organizationId(organization.getId())
                                               .location("location")
                                               .code("code")
                                               .parentId(UUID.randomUUID())
                                               .humanReadableId(department.getHumanReadableId())
                                               .build();
        when(service.get(messageNoParent.getParentId())).thenReturn(Optional.empty());
        when(organizationService.get(messageNoParent.getOrganizationId())).thenReturn(Optional.of(organization));
        var exception = assertThrows(AwaitingSynchronizationException.class, () -> provider
                .save(messageNoParent));
        assertEquals(String.format(ERROR_NOT_IN_DB_MESSAGE_FORMAT,
                                   messageNoParent.getId(),
                                   messageNoParent.getDepartmentName(),
                                   messageNoParent.getParentId(),
                                   "parent"), exception.getMessage());
        verify(mapper, never()).departmentMessageToDepartment(messageNoParent);
        verify(service, never()).saveOrUpdate(department);
        verify(organizationService).get(messageNoParent.getOrganizationId());
        verify(service).get(messageNoParent.getParentId());
    }

    @Test
    void saveOrUpdateHaveParent() {
        var parentDepartment = Department.builder()
                                         .id(UUID.randomUUID())
                                         .organizationId(organization.getId())
                                         .departmentName("departmentNameParent")
                                         .humanReadableId("humanReadableIdParent")
                                         .build();
        var messageHaveParent = DepartmentMessage.builder()
                                                 .id(department.getId())
                                                 .departmentName(department.getDepartmentName())
                                                 .organizationId(organization.getId())
                                                 .location("location")
                                                 .code("code")
                                                 .parentId(parentDepartment.getId())
                                                 .humanReadableId(department.getHumanReadableId())
                                                 .build();
        when(service.get(parentDepartment.getId())).thenReturn(Optional.of(parentDepartment));
        when(organizationService.get(messageHaveParent.getOrganizationId())).thenReturn(Optional.of(organization));
        when(mapper.departmentMessageToDepartment(messageHaveParent)).thenReturn(department);
        doNothing().when(service).saveOrUpdate(department);
        provider.save(messageHaveParent);
        verify(mapper).departmentMessageToDepartment(messageHaveParent);
        verify(service).saveOrUpdate(department);
        verify(organizationService).get(messageHaveParent.getOrganizationId());
        verify(service).get(parentDepartment.getId());
    }
}