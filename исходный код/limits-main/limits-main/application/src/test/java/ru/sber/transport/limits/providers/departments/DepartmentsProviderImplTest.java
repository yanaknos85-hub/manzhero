package ru.sber.transport.limits.providers.departments;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jooq.DSLContext;
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
import ru.sber.transport.limits.business.providers.DepartmentsProvider;
import ru.sber.transport.limits.config.EnableApplicationConfig;
import ru.sber.transport.limits.providers.departments.mapper.DepartmentsDatabaseMapperImpl;
import ru.sber.transport.limits.providers.employees.mappers.EmployeeDatabaseMapperImpl;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static ru.sber.transport.database.limits.Tables.DEPARTMENT;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка провайдера лимитов")
@JooqTest
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
@ContextConfiguration(classes = {JooqDatabaseConfig.class, DepartmentsProviderImpl.class, DepartmentsDatabaseMapperImpl.class, EmployeeDatabaseMapperImpl.class})
@EnableApplicationConfig
class DepartmentsProviderImplTest {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private DepartmentsProvider departments;

    @Test
    @DisplayName("Проверка получения департамента")
    void test_get() {
        var departmentRecord = dslContext.newRecord(DEPARTMENT);
        departmentRecord.setDepartmentName(Instancio.create(String.class));
        departmentRecord.setCode(Instancio.create(String.class));
        departmentRecord.setHumanreadableid(Instancio.create(String.class));
        departmentRecord.setId(UUID.randomUUID());

        dslContext.insertInto(DEPARTMENT)
                .set(departmentRecord)
                .execute();

        var actualOpt = departments.get(departmentRecord.getId());

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.get();

        assertSoftly(it -> {
           it.assertThat(actual.getHumanReadableId()).isEqualTo(departmentRecord.getHumanreadableid());
        });
    }

}