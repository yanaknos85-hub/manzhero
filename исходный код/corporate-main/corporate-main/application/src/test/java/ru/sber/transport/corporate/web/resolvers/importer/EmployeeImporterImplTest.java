package ru.sber.transport.corporate.web.resolvers.importer;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.web.resolvers.model.FileEmployee;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.database.model.Position;
import ru.sberbank.ditsib.corpclient.database.model.messages.Role;
import ru.sberbank.ditsib.corpclient.service.*;
import ru.sber.transport.humanreadableid.service.SQGenerator;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка импорта сотрудников")
class EmployeeImporterImplTest {

    private final EmployeeService employeeService = mock(EmployeeService.class);

    private final OrganizationService organizationService = mock(OrganizationService.class);
    private final DepartmentService departmentService = mock(DepartmentService.class);
    private final PositionService positionService = mock(PositionService.class);

    private final SQGenerator generator = mock(SQGenerator.class);

    private final RolesService roles = mock(RolesService.class);

    private final EmployeeImporterImpl
            resolver = new EmployeeImporterImpl(employeeService, departmentService, positionService, organizationService, roles, generator);

    @Test
    @DisplayName("Сохранение")
    void test_response() {
        var data = new ArrayList<FileEmployee>();
        var count = 100;

        for (var i = 0; i < count; i++) {
            var item = new FileEmployee();

            var firstName = "FirstName" + i;
            var lastName = "LastName" + i;
            var patronymic = "Patronymic" + i;

            item.setPosition("Position " + i);
            item.setDepartment("Department " + i);
            item.setSupervisorPersonalNumber("Supervisor " + i);
            item.setPhone("Phone " + i);
            item.setFullName(String.format("%s %s %s", lastName, firstName, patronymic));
            item.setPersonalNumber("Personal number " + i);
            item.setOrganization("Organization " + i);
            item.setEmail("Email " + i);
            item.setRole("Role" + i);

            data.add(item);

            var position = new Position();
            position.setName("Position " + i);

            var department = new Department();
            department.setName("Department " + i);

            var supervisor = new Employee();
            supervisor.setPersonnelNumber("Supervisor " + i);

            var organization = new Organization();
            organization.setOfficialName("Organization " + i);
            organization.getDepartments().add(department);
            organization.getPositions().add(position);

            position.setOrganization(organization);
            department.setOrganization(organization);

            when(departmentService.getDepartmentByName(organization.getId(), item.getDepartment())).thenReturn(Optional.of(department));
            when(positionService.getPosition(organization.getId(), item.getPosition())).thenReturn(Optional.of(position));
            when(organizationService.get(item.getOrganization())).thenReturn(Optional.of(organization));
            when(employeeService.getEmployeeByPersonalNumberAndOrganizationId(item.getSupervisorPersonalNumber(), organization.getId())).thenReturn(Optional.of(supervisor));
            when(roles.getByName("Role" + i)).thenReturn(Optional.of(createRole(i)));
            var savedEmployeeId = UUID.randomUUID();
            when(employeeService.saveEmployee(any(Employee.class), any())).then(inv -> {
                Employee employee = inv.getArgument(0);
                employee.setId(savedEmployeeId);
                employee.setNew(true);
                when(employeeService.getEmployeeById(savedEmployeeId)).thenReturn(employee);
                return employee;
            });
        }

        for (var item : data) {
            resolver.importData(item, Map.of(), mock(JwtAuthenticationToken.class));
        }
        var employeeCaptor = ArgumentCaptor.forClass(Employee.class);
        var roleCaptor = ArgumentCaptor.forClass(Set.class);

        verify(employeeService, times(count)).saveEmployee(employeeCaptor.capture(), roleCaptor.capture());

        var savedList = employeeCaptor.getAllValues();

        assertThat(savedList.size()).isEqualTo(count);

        for (int ex = 0, ac = 0; ex < count; ex++, ac++) {
            var actual = savedList.get(ac);
            var expected = data.get(ex);

            var firstName = actual.getFirstName();
            var lastName = actual.getLastName();
            var patronymic = actual.getPatronymic();

            assertThat(actual.getPosition().getName()).isEqualTo(expected.getPosition());
            assertThat(actual.getDepartment().getName()).isEqualTo(expected.getDepartment());
            assertThat(actual.getSupervisor().getPersonnelNumber()).isEqualTo(expected.getSupervisorPersonalNumber());
            assertThat(actual.getMobilePhone()).isEqualTo(expected.getPhone());
            assertThat(String.format("%s %s %s", lastName, firstName, patronymic)).isEqualTo(expected.getFullName());
            assertThat(actual.getPersonnelNumber()).isEqualTo(expected.getPersonalNumber());
            assertThat(actual.getDepartment().getOrganization().getOfficialName()).isEqualTo(expected.getOrganization());
        }
    }

    private Role createRole(int i) {
        var role = new Role();

        role.setName("Role" + i);
        role.setCode("Code" + i);

        return role;
    }

}