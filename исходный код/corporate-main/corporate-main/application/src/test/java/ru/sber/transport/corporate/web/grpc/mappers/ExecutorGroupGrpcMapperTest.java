package ru.sber.transport.corporate.web.grpc.mappers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.utils.TestHelper;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.database.model.messages.GeoZone;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка маппера группы исполнителей")
class ExecutorGroupGrpcMapperTest {


    ExecutorGroupGrpcMapper mapper = new ExecutorGroupGrpcMapperImpl();

    @Test
    @DisplayName("Проверка маппера группы исполнителей")
    void toGrpc() {

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

        executorGroup.setContractors(Set.of(UUID.randomUUID(), UUID.randomUUID()));

        var actual = mapper.toGrpc(executorGroup);

        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(executorGroupId.toString());
        assertThat(actual.getName()).isEqualTo("ExecutorGroupName");
        assertThat(actual.getService()).isEqualTo("ServiceName");
        assertThat(actual.getServiceLevel()).isEqualTo("ServiceLevelName");
        assertThat(actual.getActive()).isEqualTo(true);
        assertThat(actual.getCreationTime()).isEqualTo(mapper.map(creationTime));
        assertThat(actual.getUpdateTime()).isEqualTo(mapper.map(updateTime));
        assertThat(actual.getContractorsList())
                .containsExactlyInAnyOrderElementsOf(executorGroup.getContractors().stream().map(UUID::toString).toList());

        assertThat(actual.getExecutorsCount()).isEqualTo(executorGroup.getExecutors().size());
        for (int i = 0; i < actual.getExecutorsCount(); i++) {
            var actualExecutor = actual.getExecutors(i);
            var expectedExecutor = executorGroup.getExecutors().stream()
                    .filter(ex -> ex.getId().toString().equals(actualExecutor.getEmployeeId()))
                    .findFirst().get();
            assertThat(actualExecutor.getEmployeeId()).isEqualTo(expectedExecutor.getId().toString());
            assertThat(actualExecutor.getEmployeeName()).isEqualTo(mapper.getFIO(expectedExecutor));
            assertThat(actualExecutor.getEmployeePersonnelNumber()).isEqualTo(expectedExecutor.getPersonnelNumber());
            assertThat(actualExecutor.getEmployeeStatus()).isEqualTo(expectedExecutor.getActiveStatus().toString());
            assertThat(actualExecutor.getDepartmentId()).isEqualTo(expectedExecutor.getDepartment().getId().toString());
            assertThat(actualExecutor.getDepartmentName()).isEqualTo(expectedExecutor.getDepartment().getName());
            assertThat(actualExecutor.getDepartmentStatus()).isEqualTo(expectedExecutor.getDepartment().getActiveStatus().toString());
            assertThat(actualExecutor.getDepartmentHumanReadableId()).isEqualTo(expectedExecutor.getDepartment().getHumanReadableId());
        }
        // Массив организаций заказчика.
        // OrganizationExecutorGroup organizations = 15;
        assertThat(actual.getOrganizationsList()).isNotEmpty();
        assertThat(actual.getOrganizationsCount()).isEqualTo(executorGroup.getOrganizations().size());
        for (int i = 0; i < actual.getOrganizationsCount(); i++) {
            var actualOrganization = actual.getOrganizations(i);
            var expectedOrganization = executorGroup.getOrganizations()
                    .stream()
                    .filter(organization1 -> organization1.getId().toString().equals(actualOrganization.getId()))
                    .findFirst().orElse(null);
            assertThat(expectedOrganization).isNotNull();
            assertThat(actualOrganization.getId()).isEqualTo(expectedOrganization.getId().toString());
            assertThat(actualOrganization.getOfficialName()).isEqualTo(expectedOrganization.getOfficialName());
            assertThat(actualOrganization.getStatus()).isEqualTo(expectedOrganization.getStatus().toString());
        }


        // Массив подразделений заказчика.
        // DepartmentExecutorGroup departments = 16;
        assertThat(actual.getDepartmentsList()).isNotEmpty();
        assertThat(actual.getDepartmentsCount()).isEqualTo(executorGroup.getDepartments().size());
        for (int i = 0; i < actual.getDepartmentsCount(); i++) {
            var actualDepartment = actual.getDepartments(i);
            var expectedDepartment = executorGroup.getDepartments()
                    .stream()
                    .filter(department1 -> department1.getId().toString().equals(actualDepartment.getId()))
                    .findFirst().orElse(null);
            assertThat(expectedDepartment).isNotNull();
            assertThat(actualDepartment.getId()).isEqualTo(expectedDepartment.getId().toString());
            assertThat(actualDepartment.getDepartmentName()).isEqualTo(expectedDepartment.getName());
            assertThat(actualDepartment.getStatus()).isEqualTo(expectedDepartment.getActiveStatus().toString());
            assertThat(actualDepartment.getHumanReadableId()).isEqualTo(expectedDepartment.getHumanReadableId());

            assertThat(actualDepartment.getOrganizationName()).isEmpty();
            assertThat(actualDepartment.getHeadNameLast()).isEmpty();
            assertThat(actualDepartment.getHeadNameFirst()).isEmpty();
        }

        // Массив заказчиков.
        // EmployeeExecutorGroup customers = 17;
        assertThat(actual.getCustomersList()).isNotEmpty();
        assertThat(actual.getCustomersCount()).isEqualTo(executorGroup.getCustomers().size());
        for (int i = 0; i < actual.getCustomersCount(); i++) {
            var actualCustomer = actual.getCustomers(i);
            var expectedCustomer = executorGroup.getCustomers()
                    .stream()
                    .filter(customer -> customer.getId().toString().equals(actualCustomer.getId()))
                    .findFirst().orElse(null);
            assertThat(expectedCustomer).isNotNull();
            assertThat(actualCustomer.getId()).isEqualTo(expectedCustomer.getId().toString());
            assertThat(actualCustomer.getName()).isEqualTo(mapper.getFIO(expectedCustomer));
            assertThat(actualCustomer.getPersonnelNumber()).isEqualTo(expectedCustomer.getPersonnelNumber());
            assertThat(actualCustomer.getStatus()).isEqualTo(expectedCustomer.getActiveStatus().toString());

        }

        // Массив территорий заказчика.
        // GeoZoneShort geo_zones = 18;
        assertThat(actual.getGeoZonesList()).isNotEmpty();
        assertThat(actual.getGeoZonesCount()).isEqualTo(executorGroup.getGeoZones().size());
        for (int i = 0; i < actual.getGeoZonesCount(); i++) {
            var actualGeoZone = actual.getGeoZones(i);
            var expectedGeoZone = executorGroup.getGeoZones()
                    .stream()
                    .filter(geoZone -> geoZone.getId().toString().equals(actualGeoZone.getId()))
                    .findFirst().orElse(null);
            assertThat(expectedGeoZone).isNotNull();
            assertThat(actualGeoZone.getId()).isEqualTo(expectedGeoZone.getId().toString());
            assertThat(actualGeoZone.getName()).isEqualTo(expectedGeoZone.getName());

        }

        // Массив контрагентов.
        assertThat(actual.getContractorsList()).isNotEmpty();
        assertThat(actual.getContractorsCount()).isEqualTo(executorGroup.getContractors().size());
        for (int i = 0; i < actual.getContractorsCount(); i++) {
            var contractor = actual.getContractors(i);
            var expectedContractor = executorGroup.getContractors()
                    .stream()
                    .filter(conrtrector -> contractor.toString().equals(contractor))
                    .findFirst().orElse(null);
            assertThat(expectedContractor).isNotNull();

        }


    }

}