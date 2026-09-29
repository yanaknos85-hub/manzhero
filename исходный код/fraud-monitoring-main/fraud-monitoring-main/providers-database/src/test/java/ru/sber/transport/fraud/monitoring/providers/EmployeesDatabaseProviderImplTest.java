package ru.sber.transport.fraud.monitoring.providers;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.database.fraud_monitoring.Tables;
import ru.sber.transport.fraud.monitoring.providers.employees.EmployeeDatabaseProviderImpl;
import ru.sber.transport.fraud.monitoring.providers.model.TestEmployee;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка провайдера сотрудников")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class EmployeesDatabaseProviderImplTest {

    @Autowired
    private DSLContext context;

    private final EmployeesDatabaseProvider employeesProvider = new EmployeeDatabaseProviderImpl() {

        @Override
        public DSLContext context() {
            return context;
        }
    };

    @Test
    @DisplayName("Проверка сохранения сотрудника")
    void test_createOrUpdate() {
        final var source = new TestEmployee(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class)
        );

        employeesProvider.createOrUpdate(source);

        assertThat(context.fetchCount(Tables.EMPLOYEE)).isEqualTo(1);

        final var actual = context.selectFrom(Tables.EMPLOYEE).fetchSingle();
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(source.getId());
        assertThat(actual.getDepartmentId()).isEqualTo(source.getDepartmentId());
        assertThat(actual.getOrganizationId()).isEqualTo(source.getOrganizationId());
        assertThat(actual.getLastName()).isEqualTo(source.getLastName());
        assertThat(actual.getFirstName()).isEqualTo(source.getFirstName());
        assertThat(actual.getPatronymic()).isEqualTo(source.getPatronymic());
        assertThat(actual.getPositionId()).isEqualTo(source.getPositionId());
        assertThat(actual.getPersonnelNumber()).isEqualTo(source.getPersonnelNumber());
        assertThat(actual.getCostCenter()).isEqualTo(source.getCostCenter());
    }

    @Test
    @DisplayName("Проверка получения сотрудника")
    void test_get() {
        final var id = UUID.randomUUID();
        final var saved = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, id)
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.POSITION_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.LAST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.PATRONYMIC, Instancio.create(String.class))
                .returning()
                .fetchSingle();

        final var actual = employeesProvider.get(id);
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getOrganizationId()).isEqualTo(saved.getOrganizationId());
        assertThat(actual.getDepartmentId()).isEqualTo(saved.getDepartmentId());
        assertThat(actual.getLastName()).isEqualTo(saved.getLastName());
        assertThat(actual.getFirstName()).isEqualTo(saved.getFirstName());
        assertThat(actual.getPatronymic()).isEqualTo(saved.getPatronymic());
        assertThat(actual.getPositionId()).isEqualTo(saved.getPositionId());
        assertThat(actual.getPersonnelNumber()).isEqualTo(saved.getPersonnelNumber());
    }
}