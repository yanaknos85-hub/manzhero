package ru.sber.transport.tariff_fleet.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.database.dao.DepartmentRepository;
import ru.sber.transport.tariff_fleet.database.model.Department;
import ru.sber.transport.tariff_fleet.database.model.Organization;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.sber.transport.tariff_fleet.TestData.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка сервиса по работе с подразделениями")
class DepartmentServiceImplTest {

    @InjectMocks
    private DepartmentServiceImpl service;

    @Mock
    private DepartmentRepository repository;

    private Department department;
    private Organization organization;

    @BeforeEach
    void setUp() {
        organization = createOrganization1();
        department = Department.builder()
                .id(DEPARTMENT_1_ID)
                .departmentName("departmentName")
                .organizationId(ORGANIZATION_1_ID)
                .easupId("easupId")
                .humanReadableId("DT-0001-00000001")
                .build();
    }

    @Test
    void get() {
        when(repository.findById(department.getId())).thenReturn(Optional.of(department));
        assertEquals(service.get(department.getId()), Optional.of(department));
        assertEquals(service.get(UUID.randomUUID()), Optional.empty());
    }

    @Test
    void delete() {
        when(repository.save(department)).thenReturn(department);
        service.delete(department);
        verify(repository).save(department);
    }
    
    @Test
    void saveOrUpdate() {
        var updateDepartment1 = Department.builder()
                                          .id(DEPARTMENT_1_ID)
                                          .humanReadableId("DT-0001-00000021")
                                          .organizationId(ORGANIZATION_1_ID)
                                          .departmentName("departmentName21")
                                          .parentId(null)
                                          .easupId("easupId21")
                                          .build();
        var department2 = createDepartment2(organization, null);
        when(repository.findById(department.getId())).thenReturn(Optional.of(department));
        when(repository.findById(department2.getId())).thenReturn(Optional.empty());
        when(repository.save(updateDepartment1)).thenReturn(updateDepartment1);
        when(repository.save(department2)).thenReturn(department2);
        service.saveOrUpdate(updateDepartment1);
        service.saveOrUpdate(department2);
        verify(repository).findById(department.getId());
        verify(repository).findById(department2.getId());
        verify(repository).save(updateDepartment1);
        verify(repository).save(department2);
    }
}