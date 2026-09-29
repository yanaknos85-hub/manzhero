package ru.sberbank.ditsib.corpclient.database.dao;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.database.model.ExecutorGroup;
import ru.sberbank.ditsib.corpclient.service.FileService;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sberbank.ditsib.corpclient.util.ContextHelper;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка репозитория группы исполнителей")
@SpringBootTest
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@MockitoBean(types = {JwtDecoder.class, FileService.class})
public class ExecutorGroupRepositoryTest extends SharedData {

    @Autowired
    private ExecutorGroupRepository repository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private GeoZoneRepository geoZoneRepository;

    @BeforeEach
    public void saveNeededEntities() {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testOrganization2 = organizationRepository.save(testOrganization2);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment2.setOrganization(testOrganization2);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testDepartment2 = departmentRepository.save(testDepartment2);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee2.setDepartment(testDepartment2);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        testEmployee2.setOrganization(testDepartment2.getOrganization());
        testPosition1.setHumanReadableId("PS-001-1");
        testPosition2.setHumanReadableId("PS-001-2");
        testPosition1.setOrganization(testOrganization1);
        testPosition2.setOrganization(testOrganization2);
        positionRepository.save(testPosition1);
        positionRepository.save(testPosition2);

        testEmployee1 = employeeRepository.save(testEmployee1);
        testEmployee2 = employeeRepository.save(testEmployee2);

        testGeoZone1 = geoZoneRepository.save(testGeoZone1);
    }

    @AfterEach
    void drop() {
        repository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Сохранение группы исполнителей")
    void testSaveExecutorGroupCorrectly() {

        try (MockedStatic<ContextHelper> mockedStatic = Mockito.mockStatic(ContextHelper.class)) {
            mockedStatic.when(ContextHelper::getCurrentUser).thenReturn(USER1_ID);


            var excepted = ExecutorGroup.builder()
                    .id(UUID.randomUUID())
                    .departments(Set.of(testDepartment1))
                    .organizations(Set.of(testOrganization2))
                    .executors(Set.of(testEmployee1))
                    .customers(Set.of(testEmployee2))
                    .geoZones(Set.of(testGeoZone1))
                    .name("Test")
                    .humanReadableId("IT-000")
                    .service("TestService")
                    .active(true)
                    .organizationId(testOrganization1.getId())
                    .build();

            repository.saveAndFlush(excepted);

            var actual = repository.findAll().getFirst();

            assertEquals(actual.getActive(), excepted.getActive());
            assertEquals(actual.getHumanReadableId(), excepted.getHumanReadableId());
            assertEquals(actual.getAuthorId(), UUID.fromString(Objects.requireNonNull(ContextHelper.getCurrentUser())));
            assertEquals(actual.getUserId(), UUID.fromString(ContextHelper.getCurrentUser()));
            assertEquals(actual.getName(), excepted.getName());
            assertEquals(actual.getOrganizationId(), excepted.getOrganizationId());
            assertEquals(actual.getCustomers().stream().findFirst().orElseThrow().getId(),
                    excepted.getCustomers().stream().findFirst().orElseThrow().getId());
            assertEquals(actual.getCustomers().stream().findFirst().orElseThrow().getLastName(),
                    excepted.getCustomers().stream().findFirst().orElseThrow().getLastName());
            assertEquals(actual.getDepartments().stream().findFirst().orElseThrow().getId(),
                    excepted.getDepartments().stream().findFirst().orElseThrow().getId());
            assertEquals(actual.getDepartments().stream().findFirst().orElseThrow().getName(),
                    excepted.getDepartments().stream().findFirst().orElseThrow().getName());
            assertEquals(actual.getGeoZones().stream().findFirst().orElseThrow().getId(),
                    excepted.getGeoZones().stream().findFirst().orElseThrow().getId());
            assertEquals(actual.getGeoZones().stream().findFirst().orElseThrow().getName(),
                    excepted.getGeoZones().stream().findFirst().orElseThrow().getName());
            assertEquals(actual.getOrganizations().stream().findFirst().orElseThrow().getId(),
                    excepted.getOrganizations().stream().findFirst().orElseThrow().getId());
            assertEquals(actual.getOrganizations().stream().findFirst().orElseThrow().getOfficialName(),
                    excepted.getOrganizations().stream().findFirst().orElseThrow().getOfficialName());
            assertEquals(actual.getExecutors().stream().findFirst().orElseThrow().getId(),
                    excepted.getExecutors().stream().findFirst().orElseThrow().getId());
            assertEquals(actual.getExecutors().stream().findFirst().orElseThrow().getFirstName(),
                    excepted.getExecutors().stream().findFirst().orElseThrow().getFirstName());
            assertEquals(actual.getExecutors().stream().findFirst().orElseThrow().getDepartment().getId(),
                    excepted.getExecutors().stream().findFirst().orElseThrow().getDepartment().getId());
            assertEquals(actual.getExecutors().stream().findFirst().orElseThrow().getDepartment().getCode(),
                    excepted.getExecutors().stream().findFirst().orElseThrow().getDepartment().getCode());
        }
    }

    @Test
    @DisplayName("Тест удаления группы исполнителей")
    void testDeleteExecutorGroup() {
        testSaveExecutorGroupCorrectly();

        ExecutorGroup executorGroup = repository.findAll().getFirst();

        assertNotNull(executorGroup);

        repository.deleteById(executorGroup.getId());

        Optional<ExecutorGroup> deleted = repository.findById(executorGroup.getId());

        assertTrue(deleted.isEmpty());
    }

    @Test
    @DisplayName("Изменение группы исполнителей")
    void testUpdateExecutorGroupCorrectly() {
        testSaveExecutorGroupCorrectly();

        try (MockedStatic<ContextHelper> mockedStatic = Mockito.mockStatic(ContextHelper.class)) {
            mockedStatic.when(ContextHelper::getCurrentUser).thenReturn(USER2_ID);

            var actual = repository.findAll().getFirst();

            actual.setHumanReadableId("IT-003");

            repository.saveAndFlush(actual);

            assertEquals("IT-003", actual.getHumanReadableId());
            assertEquals(UUID.fromString(USER1_ID), actual.getAuthorId());
            assertEquals(UUID.fromString(USER2_ID), actual.getUserId());
        }
    }
}
