package ru.sber.transport.limits.providers.employees;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.limits.business.model.Employee;
import ru.sber.transport.limits.business.providers.EmployeeProvider;
import ru.sber.transport.limits.providers.DataCreator;
import ru.sber.transport.limits.providers.employees.mappers.EmployeeDatabaseMapperImpl;
import ru.sber.transport.limits.web.grpc.client.StateClient;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.when;
import static ru.sber.transport.database.limits.tables.Employee.EMPLOYEE;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка провайдера сотрудников")
@JooqTest
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
@ContextConfiguration(classes = {JooqDatabaseConfig.class, EmployeeProviderImpl.class, EmployeeDatabaseMapperImpl.class})
class EmployeeProviderImplTest implements DataCreator {

    @Autowired
    private DSLContext context;

    @Autowired
    private EmployeeProvider provider;

    @MockitoBean
    private StateClient stateClient;

    @AfterEach
    void afterEach() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @DisplayName("Проверка получения сотрудника")
    void test_get() {
        var employee = context.insertInto(EMPLOYEE).set(createEmployee()).returning().fetchSingle();

        var actualOpt = provider.get(employee.getUserId());

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.orElseThrow();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(employee.getId());
            it.assertThat(actual.getOrganizationId()).isEqualTo(employee.getOrganizationId());
        });
    }

    @Test
    @DisplayName("Проверка получения сотрудника из источника")
    void test_get_source() {
        var employee = createEmployee();
        var received = Instancio.create(Employee.class);

        when(stateClient.get(employee.getUserId())).thenReturn(Optional.of(received));

        assertThat(context.fetchCount(EMPLOYEE)).isEqualTo(1);

        var actualOpt = provider.get(employee.getUserId());

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.orElseThrow();

        assertThat(context.fetchCount(EMPLOYEE)).isEqualTo(2);

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(received.getId());
            it.assertThat(actual.getOrganizationId()).isEqualTo(received.getOrganizationId());
        });
    }

    @Test
    @DisplayName("Проверка получения почты сотрудников")
    void test_getEmails() {
        var newEmployee = Instancio.create(Employee.class);
        var employee1 = context.insertInto(EMPLOYEE).set(createEmployee()).returning().fetchSingle();
        var employee2 = context.insertInto(EMPLOYEE).set(createEmployee()).returning().fetchSingle();
        var employee3 = createEmployee();
        employee3.setEmail(null);
        employee3 = context.insertInto(EMPLOYEE).set(employee3).returning().fetchSingle();
        context.insertInto(EMPLOYEE).set(createEmployee()).returning().fetchSingle();

        when(stateClient.get(employee3.getId())).thenReturn(Optional.of(newEmployee));

        var emails = provider.getEmails(List.of(employee1.getId(), employee2.getId(), employee3.getId()));

        assertThat(emails).containsExactlyInAnyOrder(employee1.getEmail(), employee2.getEmail(), newEmployee.getEmail());
    }

    @Test
    @DisplayName("Проверка получения текущего сотрудника. Нет авторизации")
    void test_current_noAuthorized() {
        try {
            provider.current();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("Can't acquire current employee");
        }
    }

    @Test
    @DisplayName("Проверка получения текущего сотрудника. Нет пользователя")
    void test_current_noUser() {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("alg", "none").jti(UUID.randomUUID().toString()).build()));
        try {
            provider.current();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("Can't acquire current employee");
        }
    }

    @Test
    @DisplayName("Проверка получения текущего сотрудника. Пользователь не синхронизирован")
    void test_current_noSynchronized() {
        var id = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("alg", "none").jti(id.toString()).build()));

        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), id)
                .create();

        when(stateClient.get(id)).thenReturn(Optional.of(employee));
        assertThat(context.fetchCount(EMPLOYEE)).isEqualTo(1);
        var actual = provider.current();

        assertSoftly(it -> {
            it.assertThat(actual).isNotNull();
            it.assertThat(actual.getId()).isEqualTo(employee.getId());
            it.assertThat(actual.getOrganizationId()).isEqualTo(employee.getOrganizationId());
            it.assertThat(actual.getEmail()).isEqualTo(employee.getEmail());
        });

        assertThat(context.fetchCount(EMPLOYEE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Проверка получения текущего сотрудника")
    void test_current_userFound() {
        var employee = context.insertInto(EMPLOYEE).set(createEmployee()).returning().fetchSingle();

        var id = employee.getId();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("alg", "none").jti(id.toString()).build()));

        var actual = provider.current();

        assertSoftly(it -> {
            it.assertThat(actual).isNotNull();
            it.assertThat(actual.getId()).isEqualTo(employee.getId());
            it.assertThat(actual.getOrganizationId()).isEqualTo(employee.getOrganizationId());
            it.assertThat(actual.getEmail()).isEqualTo(employee.getEmail());
        });
    }

    @Test
    @DisplayName("Проверка получения доступа сотрудника. Нет контекста безопасности")
    void test_hasAccess_noSecurity() {
        assertThat(provider.hasAccess()).isFalse();
    }

    @Test
    @DisplayName("Проверка получения доступа сотрудника. Нет данных доступа в контексте безопасности")
    void test_hasAccess_withSecurity_noAccessData() {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("alg", "none").jti(UUID.randomUUID().toString()).build()));

        assertThat(provider.hasAccess()).isFalse();
    }

    @Test
    @DisplayName("Проверка получения доступа сотрудника. Нет доступа")
    void test_hasAccess_withSecurity_noAccess() {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("alg", "none").jti(UUID.randomUUID().toString()).claim("data_master", false).build()));

        assertThat(provider.hasAccess()).isFalse();
    }

    @Test
    @DisplayName("Проверка получения доступа сотрудника. Есть доступ")
    void test_hasAccess() {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("alg", "none").jti(UUID.randomUUID().toString()).claim("data_master", true).build()));

        assertThat(provider.hasAccess()).isTrue();
    }

}