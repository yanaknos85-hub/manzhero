package ru.sber.transport.corporate.business.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.corporate.business.Employees;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.business.model.EmployeeFilter;
import ru.sber.transport.corporate.business.model.Position;
import ru.sber.transport.corporate.business.model.PositionFilter;
import ru.sber.transport.corporate.business.providers.ApprovalsProvider;
import ru.sber.transport.corporate.business.providers.AttributeProvider;
import ru.sber.transport.corporate.business.providers.DepartmentProvider;
import ru.sber.transport.corporate.business.providers.Provider;
import ru.sber.transport.corporate.web.grpc.client.SyncClient;
import ru.sber.transport.corporate.web.model.EmployeeWebFilter;
import ru.sber.transport.dto.Page;
import ru.sber.transport.web.model.Projection;
import ru.sberbank.ditsib.request.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка работы сотрудников")
class EmployeesImplTest {

    private final Provider<Employee, EmployeeFilter> provider = mock(Provider.class);
    private final Provider<Position, PositionFilter> positionProvider = mock(Provider.class);
    private final DepartmentProvider departmentProvider = mock(DepartmentProvider.class);
    private final AttributeProvider attributeProvider = mock(AttributeProvider.class);
    private final ApprovalsProvider approvalsProvider = mock(ApprovalsProvider.class);
    private final SyncClient client = mock(SyncClient.class);
    private final EmployeeOrganizationFunction employeeOrganizationFunction = mock(EmployeeOrganizationFunction.class);
    private final Employees employees = new EmployeesImpl(provider, positionProvider, departmentProvider, attributeProvider, approvalsProvider, client, employeeOrganizationFunction);

    @Test
    @DisplayName("Проверка получения сотрудников по фильтру. СМД")
    void test_getByFilter_smd() {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("value").header("alg", "none").jti(UUID.randomUUID().toString()).claim("data_master", true).build()));

        final var filter = Instancio.create(EmployeeWebFilter.class);
        final var projection = Instancio.create(Projection.class);
        final var page = Instancio.create(Integer.class);
        final var size = Instancio.create(Integer.class);
        final var sort = Instancio.create(String.class);
        final var direction = Instancio.create(Direction.class);
        final var content = Instancio.createList(Employee.class);
        final var total = Instancio.create(Integer.class);
        final var pageRequest = PageRequest.of(page, size, Sort.Direction.valueOf(direction.name()), sort);

        final var pageData = new Page<>(new PageImpl<>(content, pageRequest, total));

        when(provider.streamAll(filter, projection, page, size, sort, direction)).thenReturn(pageData);

        final var actualPageData = employees.get(filter, projection, page, size, sort, direction);

        final var actualContent = actualPageData.getContent();
        assertThat(actualContent).hasSameElementsAs(content);

        final var actualPage = actualPageData.getPageData();
        assertThat(actualPage.number()).isEqualTo(page);
        assertThat(actualPage.size()).isEqualTo(size);

        final var actualSort = actualPageData.getSortData();
        assertThat(actualSort.field()).isEqualTo(sort);
        assertThat(actualSort.asc()).isEqualTo(Direction.ASC.equals(direction));
    }

    @Test
    @DisplayName("Проверка получения сотрудников по фильтру. Не СМД")
    void test_getByFilter_isNotSmd() {
        final UUID DEPT_1 = UUID.fromString("bb3e577c-5f8b-4121-b616-95444d256bbd");
        final UUID ORG_1 = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");

        final UUID DEPT_2 = UUID.fromString("7f8d4c3a-2b1e-4f7a-9c3d-5e2f8a7b1c3d");
        final UUID ORG_2 = UUID.fromString("27d2fd86-502b-48ef-9b60-77a9115cb953");

        SecurityContextHolder
                .getContext()
                .setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("value").header("alg", "none").jti(UUID.randomUUID().toString()).claim("not_data_master", true)
                        .build()));

        final var filter = Instancio.of(EmployeeWebFilter.class)
                .set(field(EmployeeWebFilter::getOrganizations), new ArrayList<>(List.of(ORG_1, ORG_2)))
                .set(field(EmployeeWebFilter::getDepartments), new ArrayList<>(List.of(DEPT_1, DEPT_2)))
                .create();
        final var projection = Instancio.create(Projection.class);
        final var page = Instancio.create(Integer.class);
        final var size = Instancio.create(Integer.class);
        final var sort = Instancio.create(String.class);
        final var direction = Instancio.create(Direction.class);
        final var content = Instancio.createList(Employee.class);
        final var total = Instancio.create(Integer.class);
        final var pageRequest = PageRequest.of(page, size, Sort.Direction.valueOf(direction.name()), sort);

        final var pageData = new Page<>(new PageImpl<>(content, pageRequest, total));

        final var deptOrganizations = Map.ofEntries(
                Map.entry(DEPT_1, ORG_1),
                Map.entry(DEPT_2, ORG_2)
        );

        when(employeeOrganizationFunction.apply(any(UUID.class))).thenReturn(ORG_1);
        when(departmentProvider.getOrganizations(anyList())).thenReturn(deptOrganizations);
        when(provider.streamAll(filter, projection, page, size, sort, direction)).thenReturn(pageData);

        final var actualPageData = employees.get(filter, projection, page, size, sort, direction);

        assertThat(filter.getDepartments()).hasSize(1);
        assertThat(filter.getOrganizations()).hasSize(1);

        final var actualContent = actualPageData.getContent();
        assertThat(actualContent).hasSameElementsAs(content);

        final var actualPage = actualPageData.getPageData();
        assertThat(actualPage.number()).isEqualTo(page);
        assertThat(actualPage.size()).isEqualTo(size);

        final var actualSort = actualPageData.getSortData();
        assertThat(actualSort.field()).isEqualTo(sort);
        assertThat(actualSort.asc()).isEqualTo(Direction.ASC.equals(direction));
    }
}