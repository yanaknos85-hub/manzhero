package ru.sber.transport.corporate.web.resolvers.exporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.database.model.Position;
import ru.sberbank.ditsib.corpclient.database.model.messages.Role;
import ru.sberbank.ditsib.corpclient.dto.FileExportFilterDTO;
import ru.sberbank.ditsib.corpclient.service.EmployeeService;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка экспорта сотрудников")
class EmployeeExporterImplTest {

    private final EmployeeService employeeService = mock(EmployeeService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final EmployeeExporterImpl
            resolver = new EmployeeExporterImpl(employeeService, objectMapper);

    @SneakyThrows
    @Test
    @DisplayName("Проверка экспорта данных")
    void test_exportData() {
        var organizationId = UUID.randomUUID();
        var data = new ArrayList<Employee>();
        var count = 100;

        for (var i = 0; i < count; i++) {

            var position = new Position();
            position.setName("Position " + i);

            var department = new Department();
            department.setName("Department " + i);

            var organization = new Organization();
            organization.setId(organizationId);
            organization.setOfficialName("Organization " + i);
            organization.getDepartments().add(department);
            organization.getPositions().add(position);

            var supervisor = new Employee();
            supervisor.setPersonnelNumber("Supervisor " + i);

            position.setOrganization(organization);
            department.setOrganization(organization);
            var item = new Employee();

            item.setId(UUID.randomUUID());
            item.setPosition(position);
            item.setDepartment(department);
            item.setSupervisor(supervisor);
            item.setMobilePhone("Phone " + i);
            item.setPatronymic("Patronymic " + i);
            item.setLastName("Last name " + i);
            item.setPersonnelNumber("Personal number " + i);
            item.setFirstName("First name " + i);
            item.setUserId(item.getId());
            item.setEmail("Email " + i);

            data.add(item);
        }

        when(employeeService.findAllByOrganizationId(organizationId)).thenReturn(data);

        var filter = new FileExportFilterDTO(organizationId);
        var filterStr = objectMapper.writeValueAsString(filter);
        var encodedFilter = Base64.getEncoder().encodeToString(filterStr.getBytes(StandardCharsets.UTF_8));
        var parameters = Map.of("filters", (Object) encodedFilter);
        var actualList = resolver.exportData(parameters, new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti("jti").build()));

        assertThat(actualList).hasSize(count);

        for (var i = 0; i < count; i++) {
            var actual = actualList.get(i);
            var expected = data.get(i);

            var firstName = expected.getFirstName();
            var lastName = expected.getLastName();
            var patronymic = expected.getPatronymic();

            assertThat(actual.getPosition()).isEqualTo(expected.getPosition().getName());
            assertThat(actual.getDepartment()).isEqualTo(expected.getDepartment().getName());
            assertThat(actual.getSupervisorPersonalNumber()).isEqualTo(expected.getSupervisor().getPersonnelNumber());
            assertThat(actual.getPhone()).isEqualTo(expected.getMobilePhone());
            assertThat(actual.getFullName()).isEqualTo(String.format("%s %s %s", lastName, firstName, patronymic));
            assertThat(actual.getPersonalNumber()).isEqualTo(expected.getPersonnelNumber());
            assertThat(actual.getOrganization()).isEqualTo(expected.getDepartment().getOrganization().getOfficialName());
        }
    }

    private Role createRole(int i) {
        var role = new Role();

        role.setName("Role" + i);
        role.setCode("Code" + i);

        return role;
    }

}