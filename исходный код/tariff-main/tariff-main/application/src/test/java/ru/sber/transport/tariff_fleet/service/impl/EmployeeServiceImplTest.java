package ru.sber.transport.tariff_fleet.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.database.dao.EmployeeRepository;
import ru.sber.transport.tariff_fleet.database.model.Department;
import ru.sber.transport.tariff_fleet.database.model.Employee;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.Position;
import ru.sber.transport.tariff_fleet.exception.UserNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.sber.transport.tariff_fleet.TestData.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка сервиса по работа с пользователями")
class EmployeeServiceImplTest {
    
    @InjectMocks
    private EmployeeServiceImpl service;
    @Mock
    private EmployeeRepository repository;
    private Organization organization;
    private Department department;
    private Position position;
    private Employee employee1;
    private Employee employee2;
    
    @BeforeEach
    void setUp() {
        organization = createOrganization1();
        department = createDepartment1(organization, null);
        position = createPosition1(organization);
        employee1 = createEmployee3(organization, department, position);
        employee2 = createEmployee4(organization, department, position);
    }
    
    @Test
    void get() {
        when(repository.findById(employee1.getId())).thenReturn(Optional.of(employee1));
        assertEquals(service.get(employee1.getId()), Optional.of(employee1));
        assertEquals(service.get(UUID.randomUUID()), Optional.empty());
    }
    
    @Test
    void getByUserId() {
        when(repository.findWithOrganizationByUserId(employee1.getId())).thenReturn(Optional.of(employee1));
        assertEquals(service.getByUserId(employee1.getId()), employee1);
        var userId = UUID.randomUUID();
        assertThrows(UserNotFoundException.class, () -> service.getByUserId(userId));
    }
    
    @Test
    void delete() {
        when(repository.save(employee1)).thenReturn(employee1);
        service.delete(employee1);
        verify(repository).save(employee1);
    }
    
    @Test
    void saveOrUpdate() {
        var updateEmployee1 = Employee.builder()
                                      .id(EMPLOYEE_3_ID)
                                      .personnelNumber("00000031")
                                      .patronymic("ГригорьевичUpdated")
                                      .lastName("КолесниковенкоUpdated")
                                      .firstName("АндрейUpdated")
                                      .mobilePhone("+792356822")
                                      .organization(organization)
                                      .department(department)
                                      .userId(EMPLOYEE_3_ID)
                                      .humanReadableId("US-0001-00000033")
                                      .position(position)
                                      .build();
        when(repository.findById(employee1.getId())).thenReturn(Optional.of(employee1));
        when(repository.findById(employee2.getId())).thenReturn(Optional.empty());
        when(repository.save(updateEmployee1)).thenReturn(updateEmployee1);
        when(repository.save(employee2)).thenReturn(employee2);
        service.saveOrUpdate(updateEmployee1);
        service.saveOrUpdate(employee2);
        verify(repository).findById(employee1.getId());
        verify(repository).findById(employee2.getId());
        verify(repository).save(updateEmployee1);
        verify(repository).save(employee2);
    }
}