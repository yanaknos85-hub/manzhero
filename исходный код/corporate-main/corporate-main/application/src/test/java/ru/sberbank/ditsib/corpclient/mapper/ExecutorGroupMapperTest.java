package ru.sberbank.ditsib.corpclient.mapper;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.messaging.senders.mappers.ContactMessageMapperImpl;
import ru.sber.transport.utils.TestHelper;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.database.model.messages.GeoZone;
import ru.sberbank.ditsib.corpclient.dto.ExecutorDTO;
import ru.sberbank.ditsib.corpclient.dto.NewExecutorGroupDTO;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка маппера группы исполнителей")
class ExecutorGroupMapperTest {

    private final ActiveStatusMapperImpl activeStatusMapper = new ActiveStatusMapperImpl();
    private final ContactMessageMapperImpl contactMessageMapper = new ContactMessageMapperImpl();
    private final DepartmentMapper departmentMapper = new DepartmentMapperImpl(new ActiveStatusMapperImpl());
    private final ContactMapperImpl contactMapper = new ContactMapperImpl();
    private final OrganizationMapper organizationMapper = new OrganizationMapperImpl(contactMapper);
    private final PersonalCarMapper personalCarMapper = new PersonalCarMapperImpl();
    private final AttributeMapper attributeMapper = new AttributeMapperImpl();
    private final DateMapper dateMapper = new DateMapperImpl();
    private final EmployeeMapper employeeMapper =
            new EmployeeMapperImpl(personalCarMapper,
                    attributeMapper,
                    dateMapper,
                    activeStatusMapper,
                    contactMessageMapper);
    private final GeoZoneMapper geoZoneMapper = new GeoZoneMapperImpl();
    private final ExecutorGroupMapper mapper =
            new ExecutorGroupMapperImpl(departmentMapper,
                    organizationMapper,
                    employeeMapper,
                    geoZoneMapper);


    @Test
    void toDto() {
        assertThat(mapper.toDto(null)).isNull();

        UUID executorGroupId = UUID.randomUUID();

        LocalDateTime creationTime = LocalDateTime.now().minusDays(1);
        LocalDateTime updateTime = LocalDateTime.now().minusDays(3);

        var contractors = Set.of(UUID.randomUUID(), UUID.randomUUID());

        var executorGroup = TestHelper.createExecutorGroup(executorGroupId, creationTime, updateTime, contractors);

        var organization = TestHelper.createOrganization();
        var department = TestHelper.createDepartment(organization);
        var position = TestHelper.createPosition(organization);

        Set<Employee> executors = new HashSet<>();
        for (var i = 0; i < 5; i++) {
            executors.add(TestHelper.createEmployee(department, i, position));
        }
        executorGroup.setExecutors(executors);

        Set<Organization> organizations = new HashSet<>();
        for (var i = 0; i < 3; i++) {
            organizations.add(TestHelper.createOrganization(i));
        }
        executorGroup.setOrganizations(organizations);

        Set<Department> departments = new HashSet<>();
        for (var i = 0; i < 2; i++) {
            departments.add(TestHelper.createDepartment(organization, i));
        }
        executorGroup.setDepartments(departments);

        Set<Employee> customers = new HashSet<>();
        for (var i = 0; i < 3; i++) {
            customers.add(TestHelper.createEmployee(department, i, position));
        }
        executorGroup.setCustomers(customers);

        Set<GeoZone> geoZones = new HashSet<>();
        for (var i = 0; i < 3; i++) {
            geoZones.add(TestHelper.createGeoZone(i));
        }
        executorGroup.setGeoZones(geoZones);


        var actual = mapper.toDto(executorGroup);


        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(executorGroupId);
        assertThat(actual.getName()).isEqualTo("ExecutorGroupName");
        assertThat(actual.getService()).isEqualTo("ServiceName");
        assertThat(actual.getServiceLevel()).isEqualTo("ServiceLevelName");
        assertThat(actual.getActive()).isEqualTo(true);
        assertThat(actual.getCreationTime()).isEqualTo(creationTime);
        assertThat(actual.getUpdatedAt()).isEqualTo(updateTime);
        assertThat(actual.getContractors()).containsExactlyInAnyOrderElementsOf(contractors);

        List<ExecutorDTO> actualExecutors = actual.getExecutors();
        assertThat(actualExecutors.size()).isEqualTo(executorGroup.getExecutors().size());
        for (var actualExecutor : actualExecutors) {
            var expectedExecutor = executorGroup.getExecutors().stream()
                    .filter(ex-> ex.getId().equals(actualExecutor.getEmployeeId()))
                    .findFirst().get();
            assertThat(actualExecutor.getEmployeeId()).isEqualTo(expectedExecutor.getId());
            assertThat(actualExecutor.getEmployeeName()).isEqualTo(employeeMapper.getFIO(expectedExecutor));
            assertThat(actualExecutor.getEmployeePersonnelNumber()).isEqualTo(expectedExecutor.getPersonnelNumber());
            assertThat(actualExecutor.getEmployeeStatus()).isEqualTo(expectedExecutor.getActiveStatus().toString());
            assertThat(actualExecutor.getDepartmentId()).isEqualTo(expectedExecutor.getDepartment().getId());
            assertThat(actualExecutor.getDepartmentName()).isEqualTo(expectedExecutor.getDepartment().getName());
            assertThat(actualExecutor.getDepartmentStatus()).isEqualTo(expectedExecutor.getDepartment().getActiveStatus().toString());
            assertThat(actualExecutor.getDepartmentHumanReadableId()).isEqualTo(expectedExecutor.getDepartment().getHumanReadableId());
        }
        // Массив организаций заказчика.
        // OrganizationExecutorGroup organizations = 15;
        assertThat(actual.getOrganizations()).isNotEmpty();
        assertThat(actual.getOrganizations().size()).isEqualTo(executorGroup.getOrganizations().size());
        for (int i = 0; i < actual.getOrganizations().size(); i++) {
            var actualOrganization = actual.getOrganizations().get(i);
            var expectedOrganization = executorGroup.getOrganizations()
                    .stream()
                    .filter(organization1 -> organization1.getId().equals(actualOrganization.getId()))
                    .findFirst().orElse(null);
            assertThat(expectedOrganization).isNotNull();
            assertThat(actualOrganization.getId()).isEqualTo(expectedOrganization.getId());
            assertThat(actualOrganization.getOfficialName()).isEqualTo(expectedOrganization.getOfficialName());
            assertThat(actualOrganization.getStatus().toString()).isEqualTo(expectedOrganization.getStatus().toString());
        }


        // Массив подразделений заказчика.
        // DepartmentExecutorGroup departments = 16;
        assertThat(actual.getDepartments()).isNotEmpty();
        assertThat(actual.getDepartments().size()).isEqualTo(executorGroup.getDepartments().size());
        for (int i = 0; i < actual.getDepartments().size(); i++) {
            var actualDepartment = actual.getDepartments().get(i);
            var expectedDepartment = executorGroup.getDepartments()
                    .stream()
                    .filter(department1 -> department1.getId().equals(actualDepartment.getId()))
                    .findFirst().orElse(null);
            assertThat(expectedDepartment).isNotNull();
            assertThat(actualDepartment.getId()).isEqualTo(expectedDepartment.getId());
            assertThat(actualDepartment.getDepartmentName()).isEqualTo(expectedDepartment.getName());
            assertThat(actualDepartment.getStatus().toString()).isEqualTo(expectedDepartment.getActiveStatus().toString());
            assertThat(actualDepartment.getHumanReadableId()).isEqualTo(expectedDepartment.getHumanReadableId());

              /* Нет маппинга:
               * assertThat(actualDepartment.getOrganizationName()).isEqualTo(expectedDepartment.getOrganization().getOfficialName());
               * assertThat(actualDepartment.getHeadNameLast()).isEqualTo(expectedDepartment.getHead().getLastName());
               * assertThat(actualDepartment.getHeadNameFirst()).isEqualTo(expectedDepartment.getHead().getFirstName());
               */

        }

        // Массив заказчиков.
        // EmployeeExecutorGroup customers = 17;
        assertThat(actual.getCustomers()).isNotEmpty();
        assertThat(actual.getCustomers().size()).isEqualTo(executorGroup.getCustomers().size());
        for (int i = 0; i < actual.getCustomers().size(); i++) {
            var actualCustomer = actual.getCustomers().get(i);
            var expectedCustomer = executorGroup.getCustomers()
                    .stream()
                    .filter(customer -> customer.getId().equals(actualCustomer.getId()))
                    .findFirst().orElse(null);
            assertThat(expectedCustomer).isNotNull();
            assertThat(actualCustomer.getId()).isEqualTo(expectedCustomer.getId());
            assertThat(actualCustomer.getName()).isEqualTo(employeeMapper.getFIO(expectedCustomer));
            assertThat(actualCustomer.getPersonnelNumber()).isEqualTo(expectedCustomer.getPersonnelNumber());
            assertThat(actualCustomer.getStatus().toString()).isEqualTo(expectedCustomer.getActiveStatus().toString());

        }

        // Массив территорий заказчика.
        // GeoZoneShort geo_zones = 18;
        assertThat(actual.getGeoZones()).isNotEmpty();
        assertThat(actual.getGeoZones().size()).isEqualTo(executorGroup.getGeoZones().size());
        for (int i = 0; i < actual.getGeoZones().size(); i++) {
            var actualGeoZone = actual.getGeoZones().get(i);
            var expectedGeoZone = executorGroup.getGeoZones()
                    .stream()
                    .filter(geoZone -> geoZone.getId().equals(actualGeoZone.getId()))
                    .findFirst().orElse(null);
            assertThat(expectedGeoZone).isNotNull();
            assertThat(actualGeoZone.getId()).isEqualTo(expectedGeoZone.getId());
            assertThat(actualGeoZone.getName()).isEqualTo(expectedGeoZone.getName());

        }
    }

    @Test
    void toUpdate() {
        var orgId = UUID.randomUUID();
        var contractorId = UUID.randomUUID();
        var contractorId2 = UUID.randomUUID();
        var emp1 = UUID.randomUUID();
        var emp2 = UUID.randomUUID();
        var expected = NewExecutorGroupDTO.builder()
                .name("СБ/EG01/000001")
                .service("Услуга 1")
                .serviceLevel("SERVICE1")
                .organizationId(orgId)
                .executors(List.of(emp1.toString(), emp2.toString()))
                .organizations(List.of(UUID.randomUUID().toString()))
                .departments(List.of(UUID.randomUUID().toString()))
                .customers(List.of(UUID.randomUUID().toString()))
                .geoZones(List.of(UUID.randomUUID().toString()))
                .contractors(List.of(contractorId, contractorId2))
                .additionalFeature("VIP")
                .humanReadableId("EG-001-00001")
                .active(false)
                .build();

        var actual = mapper.toUpdate(expected);

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .ignoringFields("creationTime")
                .ignoringFields("updateTime")
                .ignoringFields("authorId")
                .ignoringFields("userId")
                .ignoringFields("customers")
                .ignoringFields("departments")
                .ignoringFields("executors")
                .ignoringFields("geoZones")
                .ignoringFields("organizations")
                .isEqualTo(expected);

        assertThat(actual.getCustomers().stream()
                .map(Employee::getId)
                .filter(Objects::nonNull)
                .map(UUID::toString)
                .toList())
                .containsExactlyInAnyOrderElementsOf(expected.getCustomers());
        assertThat(actual.getDepartments().stream()
                .map(Department::getId)
                .filter(Objects::nonNull)
                .map(UUID::toString)
                .toList())
                .containsExactlyInAnyOrderElementsOf(expected.getDepartments());
        assertThat(actual.getExecutors().stream()
                .map(Employee::getId)
                .filter(Objects::nonNull)
                .map(UUID::toString)
                .toList())
                .containsExactlyInAnyOrderElementsOf(expected.getExecutors());
        assertThat(actual.getGeoZones().stream()
                .map(GeoZone::getId)
                .filter(Objects::nonNull)
                .map(UUID::toString)
                .toList())
                .containsExactlyInAnyOrderElementsOf(expected.getGeoZones());
        assertThat(actual.getOrganizations().stream()
                .map(Organization::getId)
                .filter(Objects::nonNull)
                .map(UUID::toString)
                .toList())
                .containsExactlyInAnyOrderElementsOf(expected.getOrganizations());
    }
}