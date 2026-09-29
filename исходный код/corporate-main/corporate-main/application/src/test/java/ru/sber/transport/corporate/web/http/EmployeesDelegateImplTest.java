package ru.sber.transport.corporate.web.http;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.Employees;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.business.model.EmployeeFilter;
import ru.sber.transport.corporate.web.http.mappers.*;
import ru.sber.transport.dto.Page;
import ru.sber.transport.web.api.EmployeesQueryApi;
import ru.sber.transport.web.model.ActiveStatus;
import ru.sber.transport.web.model.OrgStructureType;
import ru.sber.transport.web.model.Projection;
import ru.sberbank.ditsib.request.Direction;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка делегата сотрудников")
class EmployeesDelegateImplTest {

    private final Employees businessEmployees = mock(Employees.class);

    private final EmployeesWebMapper mapper = new EmployeesWebMapperImpl();

    private final PageMapper pageMapper = new PageMapperImpl();

    private final SortMapper sortMapper = new SortMapperImpl();

    private final EmployeesQueryApi delegate = new EmployeesDelegateImpl(businessEmployees, mapper, pageMapper, sortMapper);

    @Test
    @DisplayName("Проверка метода получения списка сотрудников")
    void test_getAll() {
        final var organizations = Instancio.createList(UUID.class);
        final var departments = Instancio.createList(UUID.class);
        final var employees = Instancio.createList(UUID.class);
        final var fullName = Instancio.create(String.class);
        final var personnelNumber = Instancio.create(String.class);
        final var humanReadableId = Instancio.create(String.class);
        final var status = Instancio.create(ActiveStatus.class);
        final var mobilePhone = Instancio.create(String.class);
        final var email = Instancio.create(String.class);
        final var orgStructureType = Instancio.create(OrgStructureType.class);
        final var projection = Instancio.create(Projection.class);
        final var page = Instancio.create(Integer.class);
        final var size = Instancio.create(Integer.class);
        final var sort = Instancio.create(String.class);
        final var direction = Instancio.create(Direction.class);
        final var total = Instancio.create(Integer.class);
        final var content = Instancio.createList(Employee.class);
        final var pageData = new Page<>(new PageImpl<>(content, PageRequest.of(page, size, Sort.Direction.valueOf(direction.name()), sort), total));

        when(businessEmployees.get(any(EmployeeFilter.class), eq(projection), eq(page), eq(size), eq(sort), eq(direction))).thenReturn(pageData);

        final var response = delegate.employeesGet(organizations, departments, employees, fullName, personnelNumber, humanReadableId, status, mobilePhone, email, orgStructureType, projection, page, size, sort, direction.name());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final var responseBody = response.getBody();

        assertThat(responseBody).isNotNull();

        final var actualContent = responseBody.getContent();

        assertThat(actualContent).hasSameSizeAs(content);

        for (int i = 0; i < content.size(); i++) {
            final var actualItem = actualContent.get(i);
            final var expectedItem = content.get(i);

            assertSoftly(it -> {
               it.assertThat(actualItem.getId()).isEqualTo(expectedItem.getId());
               it.assertThat(actualItem.getOrgStructureType().name()).isEqualTo(expectedItem.getType().name());
               it.assertThat(actualItem.getMobilePhone()).isEqualTo(expectedItem.getPhone());
               it.assertThat(actualItem.getEmail()).isEqualTo(expectedItem.getEmail());
               it.assertThat(actualItem.getHumanReadableId()).isEqualTo(expectedItem.getHumanReadableId());
               it.assertThat(actualItem.getPersonnelNumber()).isEqualTo(expectedItem.getPersonnelNumber());
               it.assertThat(actualItem.getOrganizationId()).isEqualTo(expectedItem.getOrganizationId());
               it.assertThat(actualItem.getDepartmentId()).isEqualTo(expectedItem.getDepartmentId());
               it.assertThat(actualItem.getPositionId()).isEqualTo(expectedItem.getPositionId());
            });
        }

        final var actualPage = responseBody.getPage();
        assertThat(actualPage).isNotNull();

        assertSoftly(it -> {
           it.assertThat(actualPage.getNumber()).isEqualTo(page);
           it.assertThat(actualPage.getSize()).isEqualTo(size);
        });

        final var actualSort = responseBody.getSort();
        assertThat(actualSort).isNotNull();

        assertSoftly(it -> {
           it.assertThat(actualSort.getField()).isEqualTo(sort);
           it.assertThat(actualSort.getDirection().name()).isEqualTo(direction.name());
        });
    }
    @Test
    @DisplayName("Получить")
    void test_get() {
        var userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("test").header("algo", "none").jti(userId.toString()).build()));

        var source = Instancio.create(Employee.class);

        when(businessEmployees.get(userId)).thenReturn(source);

        var actual = delegate.get();

        assertSoftly(it -> {
            it.assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
            it.assertThat(actual.getBody()).isNotNull();
            var body = actual.getBody();
            assert body != null;
            it.assertThat(body.getId()).isEqualTo(source.getId());
            it.assertThat(body.getUserId()).isEqualTo(source.getId());
            it.assertThat(body.getFirstName()).isEqualTo(source.getFirstName());
            it.assertThat(body.getLastName()).isEqualTo(source.getLastName());
            it.assertThat(body.getPatronymic()).isEqualTo(source.getPatronymic());
            it.assertThat(body.getPersonnelNumber()).isEqualTo(source.getPersonnelNumber());
            it.assertThat(body.getPositionId()).isEqualTo(source.getPositionId());
            it.assertThat(body.getMobilePhone()).isEqualTo(source.getPhone());
            it.assertThat(body.getEmail()).isEqualTo(source.getEmail());
            it.assertThat(body.getSupervisorId()).isEqualTo(source.getSupervisorId());
            it.assertThat(body.getDepartmentId()).isEqualTo(source.getDepartmentId());
            it.assertThat(body.getOrganizationId()).isEqualTo(source.getOrganizationId());
            it.assertThat(body.getPositionName()).isEqualTo(source.getPositionName());
            it.assertThat(body.getStatus().name()).isEqualTo(source.getStatus().name());
            it.assertThat(body.getAttributes()).hasSameSizeAs(source.getAttributes());
            it.assertThat(body.getHumanReadableId()).isEqualTo(source.getHumanReadableId());
            it.assertThat(body.getRoles()).isEmpty();
            it.assertThat(body.getApprovals()).isEqualTo(source.getApprovals());
            it.assertThat(body.isIsDepartmentHead()).isEqualTo(source.isDepartmentHead());
            it.assertThat(body.getManagedDepartments()).hasSameElementsAs(source.getManagedDepartments());
            it.assertThat(body.isConsent()).isEqualTo(source.isConsent());
            it.assertThat(body.getOrgStructureType().name()).isEqualTo(source.getStructureType().name());
        });
    }

}