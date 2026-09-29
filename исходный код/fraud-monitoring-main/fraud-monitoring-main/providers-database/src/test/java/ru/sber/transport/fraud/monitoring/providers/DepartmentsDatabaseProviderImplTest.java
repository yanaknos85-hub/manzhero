package ru.sber.transport.fraud.monitoring.providers;

import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
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
import ru.sber.transport.fraud.monitoring.providers.department.DepartmentsDatabaseProviderImpl;
import ru.sber.transport.fraud.monitoring.providers.model.TestDepartment;
import ru.sber.transport.fraud.monitoring.providers.model.TestEmployee;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка провайдера подразделений")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class DepartmentsDatabaseProviderImplTest {
    @Autowired
    private DSLContext context;

    private final DepartmentsDatabaseProvider departmentsProvider = new DepartmentsDatabaseProviderImpl() {
        @Override
        public DSLContext context() {
            return DepartmentsDatabaseProviderImplTest.this.context;
        }
    };

    @Test
    @DisplayName("Проверка сохранения подразделения")
    void test_createOrUpdate() {
        final var source = new TestDepartment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instancio.create(String.class),
                Instancio.create(String.class)
        );
        final var employee = new TestEmployee(
                source.getHeadId(),
                source.getId(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class)
        );

        departmentsProvider.createOrUpdate(source);

        assertThat(context.fetchCount(Tables.DEPARTMENT)).isEqualTo(1);

        final var actual = context.selectFrom(Tables.DEPARTMENT).fetchSingle();
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(source.getId());
        assertThat(actual.getHeadId()).isEqualTo(source.getHeadId());
        assertThat(actual.getParentId()).isEqualTo(source.getParentId());
        assertThat(actual.getName()).isEqualTo(source.getName());
        assertThat(actual.getCode()).isEqualTo(source.getCode());
    }

    @Test
    @DisplayName("Проверка сохранения подразделения с null")
    void test_createOrUpdate_when_source_is_null() {
        assertThat(departmentsProvider.createOrUpdate(null)).isNull();
    }

    @Test
    @DisplayName("Проверка получения подразделения")
    void test_get() {
        final var id = UUID.randomUUID();
        final var headId = UUID.randomUUID();
        final var parentId = UUID.randomUUID();
        context.insertInto(Tables.DEPARTMENT)
                .set(Tables.DEPARTMENT.ID, id)
                .set(Tables.DEPARTMENT.HEAD_ID, headId)
                .set(Tables.DEPARTMENT.CODE, Instancio.create(String.class))
                .set(Tables.DEPARTMENT.NAME, Instancio.create(String.class))
                .set(Tables.DEPARTMENT.PARENT_ID, parentId)
                .execute();

        final var actual = departmentsProvider.get(id);
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getHeadId()).isEqualTo(headId);
        assertThat(actual.getParentId()).isEqualTo(parentId);
        assertThat(actual.getCode()).isNotNull();
        assertThat(actual.getName()).isNotNull();
    }
}