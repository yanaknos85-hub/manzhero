package ru.sber.transport.limits.providers.database.departments;

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
import ru.sber.transport.limits.providers.Departments;
import ru.sber.transport.limits.providers.database.TestDepartment;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.sber.transport.database.limits.Tables.DEPARTMENT;

@JooqTest
@Transactional
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка провайдера расходов")
class DepartmentsImplTest {

    @Autowired
    DSLContext context;

    private final Departments additional = mock(Departments.class);

    private final Departments departments = new DepartmentsImpl(additional) {
        @Override
         public DSLContext context() {
            return context;
        }
    };

    @AfterEach
    void cleanup() {
        context.deleteFrom(DEPARTMENT).execute();
    }

    @Test
    @DisplayName("Проверка получения подразделений")
    void test_get() {
        final var employee = context.insertInto(DEPARTMENT)
                .set(DEPARTMENT.ID, UUID.randomUUID())
                .set(DEPARTMENT.PARENT_ID, UUID.randomUUID())
                .set(DEPARTMENT.DEPARTMENT_HEAD_ID, UUID.randomUUID())
                .set(DEPARTMENT.DEPARTMENT_NAME, Instancio.create(String.class))
                .set(DEPARTMENT.CODE, Instancio.create(String.class))
                .set(DEPARTMENT.HUMANREADABLEID, Instancio.create(String.class))
                .set(DEPARTMENT.ORGANIZATION_ID, UUID.randomUUID())
                .returning()
                .fetchSingle();

        final var actual = departments.get(employee.getId());

        assertSoftly(it -> {
           it.assertThat(actual.id()).isEqualTo(employee.getId());
           it.assertThat(actual.parentId()).isEqualTo(employee.getParentId());
        });
    }

    @Test
    @DisplayName("Проверка получения сотрудников. Запрос извне")
    void test_get_withRequest() {
        final var employee = Instancio.create(TestDepartment.class);

        when(additional.get(employee.id())).thenReturn(employee);

        final var actual = departments.get(employee.id());

        assertSoftly(it -> {
           it.assertThat(actual.id()).isEqualTo(employee.id());
           it.assertThat(actual.parentId()).isEqualTo(employee.parentId());
        });

        final var dbActual = context.fetch(DEPARTMENT).getLast();

        assertSoftly(it -> {
            it.assertThat(dbActual.getId()).isEqualTo(employee.id());
            it.assertThat(dbActual.getParentId()).isEqualTo(employee.parentId());
        });
    }

}