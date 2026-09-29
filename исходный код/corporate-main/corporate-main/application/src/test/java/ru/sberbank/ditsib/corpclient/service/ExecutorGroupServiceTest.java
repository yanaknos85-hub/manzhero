package ru.sberbank.ditsib.corpclient.service;

import io.qameta.allure.Feature;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.database.dao.*;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sberbank.ditsib.corpclient.util.ContextHelper;

import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка сервиса групы исполнителей")
@SpringBootTest(properties = { "spring.jpa.show-sql=true" })
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@Transactional
@ActiveProfiles("test")
class ExecutorGroupServiceTest extends SharedData {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private ExecutorGroupService executorGroupService;

    @Autowired
    private GeoZoneRepository geoZoneRepository;

    @Autowired
    private ExecutorGroupRepository executorGroupRepository;

    @Test
    void getExecutorGroup() {

        String service = "CARGO_TRANSPORTATION";

        TestData testData = createTestData();

        ExecutorGroup result
                = executorGroupService.getExecutorGroupByEmployeeId(testData.employee.getId(), Collections.emptyList() , service);

        assertNull(result);

        try (MockedStatic<ContextHelper> mockedStatic = Mockito.mockStatic(ContextHelper.class)) {
            mockedStatic.when(ContextHelper::getCurrentUser).thenReturn(USER1_ID);
            executorGroupRepository.saveAndFlush(ExecutorGroup.builder()
                    .id(UUID.randomUUID())
                    .organizations(Set.of(testData.organization1))
                    .executors(Set.of(testData.emps.get(9)))
                    .geoZones(Set.of(testGeoZone1))
                    .name("Name22")
                    .humanReadableId("ByOrg-00022")
                    .service("EMPLOYEE_TRANSPORTATION")
                    .active(true)
                    .organizationId(testData.organization1.getId())
                    .build());
        }

        result = executorGroupService.getExecutorGroupByEmployeeId(testData.employee.getId(), Collections.emptyList() , service);
        assertNull(result);

        try (MockedStatic<ContextHelper> mockedStatic = Mockito.mockStatic(ContextHelper.class)) {
            mockedStatic.when(ContextHelper::getCurrentUser).thenReturn(USER1_ID);
            executorGroupRepository.saveAndFlush(ExecutorGroup.builder()
                    .id(UUID.randomUUID())
                    .organizations(Set.of(testData.organization1))
                    .executors(Set.of(testData.emps.get(9)))
                    .geoZones(Set.of(testGeoZone1))
                    .name("Name2")
                    .humanReadableId("ByOrg-0002")
                    .service(service)
                    .active(true)
                    .organizationId(testData.organization1.getId())
                    .build());
        }

        result = executorGroupService.getExecutorGroupByEmployeeId(testData.employee.getId(), Collections.emptyList() , service);
        assertThat(result.getHumanReadableId()).isEqualTo( "ByOrg-0002");

        try (MockedStatic<ContextHelper> mockedStatic = Mockito.mockStatic(ContextHelper.class)) {
            mockedStatic.when(ContextHelper::getCurrentUser).thenReturn(USER1_ID);
            executorGroupRepository.saveAndFlush(ExecutorGroup.builder()
                    .id(UUID.randomUUID())
                    .organizations(Set.of(testData.organization1))
                    .departments(Set.of(testData.department1))
                    .executors(Set.of(testData.emps.get(9)))
                    .geoZones(Set.of(testGeoZone1))
                    .name("Name3")
                    .humanReadableId("ByDep-0002")
                    .service(service)
                    .active(true)
                    .organizationId(testData.organization1.getId())
                    .build());
        }

        result = executorGroupService.getExecutorGroupByEmployeeId(testData.employee.getId(), Collections.emptyList() , service);
        assertThat(result.getHumanReadableId()).isEqualTo( "ByDep-0002");

        try (MockedStatic<ContextHelper> mockedStatic = Mockito.mockStatic(ContextHelper.class)) {
            mockedStatic.when(ContextHelper::getCurrentUser).thenReturn(USER1_ID);
            executorGroupRepository.saveAndFlush(ExecutorGroup.builder()
                    .id(UUID.randomUUID())
                    .organizations(Set.of(testData.organization1))
                    .departments(Set.of(testData.department1))
                    .executors(Set.of(testData.emps.get(9)))
                    .geoZones(Set.of(testGeoZone1))
                    .name("Name4")
                    .humanReadableId("ByCust-0002")
                    .customers(Set.of(testData.employee))
                    .service(service)
                    .active(true)
                    .organizationId(testData.organization1.getId())
                    .build());
        }

        result = executorGroupService.getExecutorGroupByEmployeeId(testData.employee.getId(), Collections.emptyList() , service);
        assertThat(result.getHumanReadableId()).isEqualTo( "ByCust-0002");

        try (MockedStatic<ContextHelper> mockedStatic = Mockito.mockStatic(ContextHelper.class)) {
            mockedStatic.when(ContextHelper::getCurrentUser).thenReturn(USER1_ID);
            executorGroupRepository.saveAndFlush(ExecutorGroup.builder()
                    .id(UUID.randomUUID())
                    .organizations(Set.of(testData.organization1))
                    .departments(Set.of(testData.department1))
                    .executors(Set.of(testData.emps.get(9)))
                    .geoZones(Set.of(testGeoZone2))
                    .name("Name5")
                    .humanReadableId("ByGeo-0002")
                    .customers(Set.of(testData.employee))
                    .service(service)
                    .active(true)
                    .organizationId(testData.organization1.getId())
                    .build());
        }
        result = executorGroupService.getExecutorGroupByEmployeeId(testData.employee.getId(), Arrays.asList(testGeoZone2.getId()) , service);
        assertThat(result.getHumanReadableId()).isEqualTo( "ByGeo-0002");

        executorGroupRepository.deleteAll();

        try (MockedStatic<ContextHelper> mockedStatic = Mockito.mockStatic(ContextHelper.class)) {
            mockedStatic.when(ContextHelper::getCurrentUser).thenReturn(USER1_ID);
            executorGroupRepository.saveAndFlush(ExecutorGroup.builder()
                    .id(UUID.randomUUID())
                    .organizations(Set.of(testData.organization1))
                    .departments(Set.of(testData.department))
                    .executors(Set.of(testData.emps.get(9)))
                    .geoZones(Set.of(testGeoZone1))
                    .name("Name4")
                    .humanReadableId("ByCust-0003")
                    .customers(Set.of(testData.employee))
                    .service(service)
                    .active(true)
                    .organizationId(testData.organization1.getId())
                    .build());
        }

        result = executorGroupService.getExecutorGroupByEmployeeId(testData.employee.getId(), Collections.emptyList() , service);
        assertThat(result.getHumanReadableId()).isEqualTo( "ByCust-0003");

        executorGroupRepository.deleteAll();

        try (MockedStatic<ContextHelper> mockedStatic = Mockito.mockStatic(ContextHelper.class)) {
            mockedStatic.when(ContextHelper::getCurrentUser).thenReturn(USER1_ID);
            executorGroupRepository.saveAndFlush(ExecutorGroup.builder()
                    .id(UUID.randomUUID())
                    .organizations(Set.of(testData.organization1))
                    .departments(Set.of())
                    .executors(Set.of(testData.emps.get(9)))
                    .geoZones(Set.of(testGeoZone1))
                    .name("Name4")
                    .humanReadableId("ByOrg-0003")
                    .customers(Set.of())
                    .service(service)
                    .active(true)
                    .organizationId(testData.organization1.getId())
                    .build());
        }

        result = executorGroupService.getExecutorGroupByEmployeeId(
                testData.employee.getId(),
                Collections.emptyList() ,
                service);
        assertThat(result.getHumanReadableId()).isEqualTo( "ByOrg-0003");

    }

    @NotNull
    private TestData createTestData() {
        var organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("name");
        organization.setMsrn("msrn");
        organization.setTid("tin");
        organization = organizationRepository.save(organization);

        var organization1 = new Organization();
        organization1.setAddress("address1");
        organization1.setOfficialName("name1");
        organization1.setMsrn("msrn1");
        organization1.setTid("tin1");
        organization1 = organizationRepository.save(organization1);

        var department = new Department();
        department.setName("Department");
        department.setCode("Code");
        department.setOrganization(organization);
        department.setHumanReadableId("HRD");
        department.setUpdateTime(OffsetDateTime.now());
        department = departmentRepository.save(department);

        var department1 = new Department();
        department1.setName("Department1");
        department1.setCode("Code1");
        department1.setOrganization(organization1);
        department1.setHumanReadableId("HRD1");
        department1.setUpdateTime(OffsetDateTime.now());
        department1 = departmentRepository.save(department1);

        var position = new Position();
        position.setName("Position");
        position.setOrganization(organization);
        position.setHumanReadableId("HRP");
        position = positionRepository.save(position);

        var position1 = new Position();
        position1.setName("Position1");
        position1.setOrganization(organization1);
        position1.setHumanReadableId("HRP1");
        position1 = positionRepository.save(position1);

        geoZoneRepository.save(testGeoZone1);
        geoZoneRepository.save(testGeoZone2);

        var emps = new ArrayList<Employee>();

        for (var i = 0; i < 11; i++) {
            var employee = new Employee();
            employee.setId(UUID.randomUUID());
            employee.setNew(true);
            employee.setDepartment(department);
            employee.setLastName("LastName%02d".formatted(i));
            employee.setFirstName("FirstName%02d".formatted(i));
            employee.setPersonnelNumber("PersonnelNumber%02d".formatted(20 - i));
            employee.setPatronymic("Patronymic%02d".formatted(i));
            employee.setHumanReadableId("HumanReadable%02d".formatted(i));
            employee.setMobilePhone("Mobile%02d".formatted(i));
            employee.setEmail("Email%02d".formatted(i));
            employee.setPosition(position);
            employee.setOrganization(department.getOrganization());
            employee.setUpdateTime(OffsetDateTime.now());

            emps.add(employeeRepository.save(employee));
        }

        var employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setNew(true);
        employee.setDepartment(department1);
        employee.setLastName("LastName%02d".formatted(30));
        employee.setFirstName("FirstName%02d".formatted(30));
        employee.setPersonnelNumber("PersonnelNumber%02d".formatted(30));
        employee.setPatronymic("Patronymic%02d".formatted(30));
        employee.setHumanReadableId("HumanReadable%02d".formatted(30));
        employee.setMobilePhone("Mobile%02d".formatted(30));
        employee.setEmail("Email%02d".formatted(30));
        employee.setPosition(position1);
        employee.setOrganization(department1.getOrganization());
        employee.setUpdateTime(OffsetDateTime.now());

        emps.add(employeeRepository.save(employee));

        try (MockedStatic<ContextHelper> mockedStatic = Mockito.mockStatic(ContextHelper.class)) {
            mockedStatic.when(ContextHelper::getCurrentUser).thenReturn(USER1_ID);

            executorGroupRepository.saveAndFlush(ExecutorGroup.builder()
                    .id(UUID.randomUUID())
                    .departments(Set.of(department))
                    .organizations(Set.of(organization))
                    .executors(Set.of(emps.get(8)))
                    .customers(Set.of(emps.getFirst()))
                    .geoZones(Set.of(testGeoZone1))
                    .name("Name0")
                    .humanReadableId("IT-0000")
                    .service("TestService0")
                    .active(true)
                    .organizationId(organization.getId())
                    .build());

            executorGroupRepository.saveAndFlush(ExecutorGroup.builder()
                    .id(UUID.randomUUID())
                    .departments(Set.of(department))
                    .organizations(Set.of(organization))
                    .executors(Set.of(emps.get(7)))
                    .geoZones(Set.of(testGeoZone1))
                    .name("Name1")
                    .humanReadableId("IT-0001")
                    .service("TestService1")
                    .active(true)
                    .organizationId(organization.getId())
                    .build());

        }
        TestData testData = new TestData(organization, organization1, department, department1, emps, employee);
        return testData;
    }

    private record TestData(Organization organization, Organization organization1, Department department, Department department1, ArrayList<Employee> emps, Employee employee) {
    }
}