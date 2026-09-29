package ru.sber.transport.limits.providers.database.employees;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.limits.model.Employee;
import ru.sber.transport.limits.providers.Employees;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.sber.transport.database.limits.Tables.EMPLOYEE;

@JooqTest
@Transactional
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка провайдера расходов")
class EmployeesImplTest {

    @Autowired
    DSLContext context;

    private final Employees additional = mock(Employees.class);

    private final Employees employees = new EmployeesImpl(additional) {
        @Override
         public DSLContext context() {
            return context;
        }
    };

    @AfterEach
    void cleanup() {
        context.deleteFrom(EMPLOYEE).execute();
    }

    @Test
    @DisplayName("Проверка получения сотрудников")
    void test_get() {
        final var employee = context.insertInto(EMPLOYEE)
                .set(EMPLOYEE.ID, UUID.randomUUID())
                .set(EMPLOYEE.FIRST_NAME, "Проверка получения сотрудников")
                .set(EMPLOYEE.LAST_NAME, Instancio.create(String.class))
                .set(EMPLOYEE.EMAIL, Instancio.create(String.class))
                .set(EMPLOYEE.USER_ID, UUID.randomUUID())
                .set(EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .set(EMPLOYEE.ACTIVE, Instancio.create(Boolean.class))
                .set(EMPLOYEE.ORGANIZATION_ID, UUID.randomUUID())
                .set(EMPLOYEE.PATRONYMIC, Instancio.create(String.class))
                .set(EMPLOYEE.PERSONNEL_NUMBER, Instancio.create(String.class))
                .set(EMPLOYEE.HUMANREADABLEID, Instancio.create(String.class))
                .set(EMPLOYEE.POSITION_ID, UUID.randomUUID())
                .set(EMPLOYEE.SUPERVISOR_ID, UUID.randomUUID())
                .returning()
                .fetchSingle();

        final var actual = employees.get(employee.getId());

        assertSoftly(it -> {
           it.assertThat(actual.id()).isEqualTo(employee.getId());
           it.assertThat(actual.departmentId()).isEqualTo(employee.getDepartmentId());
        });
    }

    @Test
    @DisplayName("Проверка получения сотрудников. Запрос извне")
    void test_get_withRequest() {
        final var employee = Instancio.create(TestEmployee.class);

        when(additional.get(employee.id())).thenReturn(employee);

        final var actual = employees.get(employee.id());

        assertSoftly(it -> {
           it.assertThat(actual.id()).isEqualTo(employee.id());
           it.assertThat(actual.departmentId()).isEqualTo(employee.departmentId());
        });

        final var dbActual = context.fetch(EMPLOYEE).getLast();

        assertSoftly(it -> {
            it.assertThat(dbActual.getId()).isEqualTo(employee.id());
            it.assertThat(dbActual.getDepartmentId()).isEqualTo(employee.departmentId());
        });
    }

    private record TestEmployee(UUID id, UUID departmentId) implements Employee {}

}