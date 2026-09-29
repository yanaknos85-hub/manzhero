package ru.sber.transport.tariff_fleet.provider.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.database.model.Department;
import ru.sber.transport.tariff_fleet.database.model.Employee;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.Position;
import ru.sber.transport.tariff_fleet.exception.AwaitingSynchronizationException;
import ru.sber.transport.tariff_fleet.mapper.EmployeeMapper;
import ru.sber.transport.tariff_fleet.service.DepartmentService;
import ru.sber.transport.tariff_fleet.service.EmployeeService;
import ru.sber.transport.tariff_fleet.service.OrganizationService;
import ru.sber.transport.tariff_fleet.service.PositionService;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static ru.sber.transport.tariff_fleet.provider.impl.EmployeeProviderImpl.ERROR_NOT_IN_DB_MESSAGE_FORMAT;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка провайдера сотрудников")
class EmployeeProviderImplTest {
    
    @InjectMocks
    private EmployeeProviderImpl provider;
    @Mock
    private EmployeeService service;
    @Mock
    private EmployeeMapper mapper;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private PositionService positionService;
    private Department department;
    private Position position;
    private Employee employee;
    private EmployeeMessage message;
    private Organization organization;
    
    @BeforeEach
    void setup() {
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
        position = Position.builder()
                           .id(UUID.randomUUID())
                           .organizationId(organization.getId())
                           .positionName("positionName")
                           .build();
        employee = Employee.builder()
                           .id(UUID.randomUUID())
                           .department(department)
                           .position(position)
                           .lastName("lastName")
                           .firstName("firstName")
                           .mobilePhone("mobilePhone")
                           .patronymic("patronymic")
                           .department(department)
                           .humanReadableId("humanReadableId")
                           .personnelNumber("personnelNumber")
                           .userId(UUID.randomUUID())
                           .organization(organization)
                           .build();
        message = EmployeeMessage.builder()
                                 .id(employee.getId())
                                 .personnelNumber(employee.getPersonnelNumber())
                                 .mobilePhone(employee.getMobilePhone())
                                 .patronymic(employee.getPatronymic())
                                 .lastName(employee.getLastName())
                                 .firstName(employee.getFirstName())
                                 .positionId(position.getId())
                                 .userId(employee.getUserId())
                                 .departmentId(department.getId())
                                 .organizationId(organization.getId())
                                 .humanReadableId(employee.getHumanReadableId())
                                 .organizationId(organization.getId())
                                 .build();
    }
    
    @Test
    void delete() {
        var notExistId = UUID.randomUUID();
        var message2 = EmployeeMessage.builder()
                                      .id(notExistId)
                                      .personnelNumber(employee.getPersonnelNumber())
                                      .mobilePhone(employee.getMobilePhone())
                                      .patronymic(employee.getPatronymic())
                                      .lastName(employee.getLastName())
                                      .firstName(employee.getFirstName())
                                      .positionId(position.getId())
                                      .userId(employee.getUserId())
                                      .departmentId(department.getId())
                                      .organizationId(organization.getId())
                                      .humanReadableId(employee.getHumanReadableId())
                                      .organizationId(organization.getId())
                                      .build();
        when(service.get(message.getId())).thenReturn(Optional.of(employee));
        when(service.get(notExistId)).thenReturn(Optional.empty());
        doNothing().when(service).delete(employee);
        provider.delete(message);
        provider.delete(message2);
        verify(service).get(message.getId());
        verify(service).get(message2.getId());
        verify(service).delete(employee);
        verify(service).delete(any());
    }
    
    @Test
    void saveOrUpdate() {
        when(mapper.employeeMessageToEmployee(message)).thenReturn(employee);
        when(organizationService.get(message.getOrganizationId())).thenReturn(Optional.of(organization));
        when(departmentService.get(message.getDepartmentId())).thenReturn(Optional.of(department));
        when(positionService.get(message.getPositionId())).thenReturn(Optional.of(position));
        doNothing().when(service).saveOrUpdate(employee);
        provider.save(message);
        verify(mapper).employeeMessageToEmployee(message);
        verify(service).saveOrUpdate(employee);
        verify(organizationService).get(message.getOrganizationId());
        verify(departmentService).get(message.getDepartmentId());
        verify(positionService).get(message.getPositionId());
    }
    
    @Test
    void saveNoOrganization() {
        var messageNoOrganization = EmployeeMessage.builder()
                                                   .id(employee.getId())
                                                   .personnelNumber(employee.getPersonnelNumber())
                                                   .mobilePhone(employee.getMobilePhone())
                                                   .patronymic(employee.getPatronymic())
                                                   .lastName(employee.getLastName())
                                                   .firstName(employee.getFirstName())
                                                   .positionId(position.getId())
                                                   .userId(employee.getUserId())
                                                   .departmentId(department.getId())
                                                   .organizationId(null)
                                                   .humanReadableId(employee.getHumanReadableId())
                                                   .build();
        provider.save(messageNoOrganization);
        verify(mapper, never()).employeeMessageToEmployee(messageNoOrganization);
        verify(service, never()).saveOrUpdate(employee);
        verify(organizationService, never()).get(messageNoOrganization.getOrganizationId());
        verify(departmentService, never()).get(messageNoOrganization.getDepartmentId());
        verify(positionService, never()).get(messageNoOrganization.getPositionId());
    }
    
    @Test
    void saveNoOrganizationInDatabase() {
        when(organizationService.get(message.getOrganizationId())).thenReturn(Optional.empty());
        var exception = assertThrows(AwaitingSynchronizationException.class, () -> provider
                .save(message));
        assertEquals(String.format(ERROR_NOT_IN_DB_MESSAGE_FORMAT,
                                   message.getId(),
                                   message.getPersonnelNumber(),
                                   message.getPositionId(),
                                   message.getOrganizationId(),
                                   message.getDepartmentId(),
                                   "organization"), exception.getMessage());
        verify(mapper, never()).employeeMessageToEmployee(message);
        verify(service, never()).saveOrUpdate(employee);
        verify(organizationService).get(message.getOrganizationId());
        verify(departmentService, never()).get(message.getDepartmentId());
        verify(positionService, never()).get(message.getPositionId());
    }
    
    @Test
    void saveNoDepartmentInDatabase() {
        when(organizationService.get(message.getOrganizationId())).thenReturn(Optional.of(organization));
        when(departmentService.get(message.getDepartmentId())).thenReturn(Optional.empty());
        var exception = assertThrows(AwaitingSynchronizationException.class, () -> provider
                .save(message));
        assertEquals(String.format(ERROR_NOT_IN_DB_MESSAGE_FORMAT,
                                   message.getId(),
                                   message.getPersonnelNumber(),
                                   message.getPositionId(),
                                   message.getOrganizationId(),
                                   message.getDepartmentId(),
                                   "department"), exception.getMessage());
        verify(mapper, never()).employeeMessageToEmployee(message);
        verify(service, never()).saveOrUpdate(employee);
        verify(organizationService).get(message.getOrganizationId());
        verify(departmentService).get(message.getDepartmentId());
        verify(positionService, never()).get(message.getPositionId());
    }
    
    @Test
    void saveNoPositionInDatabase() {
        when(organizationService.get(message.getOrganizationId())).thenReturn(Optional.of(organization));
        when(departmentService.get(message.getDepartmentId())).thenReturn(Optional.of(department));
        when(positionService.get(message.getPositionId())).thenReturn(Optional.empty());
        var exception = assertThrows(AwaitingSynchronizationException.class, () -> provider
                .save(message));
        assertEquals(String.format(ERROR_NOT_IN_DB_MESSAGE_FORMAT,
                                   message.getId(),
                                   message.getPersonnelNumber(),
                                   message.getPositionId(),
                                   message.getOrganizationId(),
                                   message.getDepartmentId(),
                                   "position"), exception.getMessage());
        verify(mapper, never()).employeeMessageToEmployee(message);
        verify(service, never()).saveOrUpdate(employee);
        verify(organizationService).get(message.getOrganizationId());
        verify(departmentService).get(message.getDepartmentId());
        verify(positionService).get(message.getPositionId());
    }
}