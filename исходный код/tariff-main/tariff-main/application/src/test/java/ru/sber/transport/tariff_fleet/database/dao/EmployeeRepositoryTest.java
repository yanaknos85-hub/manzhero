package ru.sber.transport.tariff_fleet.database.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.database.model.Employee;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.sber.transport.tariff_fleet.TestData.*;

@EmbeddedPostgres
@SpringBootTest
class EmployeeRepositoryTest {

    private Employee employee1;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        var organization = organizationRepository.save(createOrganization1());
        var position = positionRepository.save(createPosition1(organization));
        var department = departmentRepository.save(createDepartment1(organization, null));
        employee1 = employeeRepository.save(createEmployee1(organization, department, position));
    }


    @Test
    void findWithOrganizationByUserId() {
        assertEquals(Optional.of(employee1), employeeRepository.findWithOrganizationByUserId(employee1.getUserId()));
        assertEquals(Optional.empty(), employeeRepository.findWithOrganizationByUserId(UUID.randomUUID()));
    }
}