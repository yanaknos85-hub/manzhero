package ru.sber.transport.corporate.providers.approvals;

import io.qameta.allure.Feature;
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
import ru.sber.transport.corporate.business.providers.ApprovalsProvider;
import ru.sber.transport.corporate.providers.attributes.mappers.AttributeDatabaseMapperImpl;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.utils.DataCreator;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sber.transport.database.corporate.Tables.*;
import static ru.sber.transport.database.corporate.tables.Organization.ORGANIZATION;
import static ru.sber.transport.database.corporate.tables.Position.POSITION;
import static ru.sber.transport.database.corporate_approvals.tables.Approvals.APPROVALS;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка провайдера согласований")
@JooqTest
@ContextConfiguration(classes = {JooqDatabaseConfig.class, ApprovalsProviderImpl.class, AttributeDatabaseMapperImpl.class})
@ActiveProfiles("test")
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
class ApprovalsProviderImplTest implements DataCreator {

    @Autowired
    private DSLContext context;

    @Autowired
    private ApprovalsProvider provider;

    @Test
    @DisplayName("Получение количества согласований")
    void test_count_by_employee() {
        var organization = context.insertInto(ORGANIZATION).set(createOrganizationRecord(0)).returning().fetchSingle();
        var position = context.insertInto(POSITION).set(createPositionRecord(0, organization.getId())).returning().fetchSingle();
        var department = context.insertInto(DEPARTMENT).set(createDepartmentRecord(0, organization.getId())).returning().fetchSingle();
        var employee = context.insertInto(EMPLOYEE).set(createEmployeeRecord(0, organization.getId(), department.getId(), position.getId())).returning().fetchSingle();
        var employee2 = context.insertInto(EMPLOYEE).set(createEmployeeRecord(1, organization.getId(), department.getId(), position.getId())).returning().fetchSingle();

        IntStream.range(0, 100)
                .mapToObj(it -> createApprovalRecord(it % 2 == 0 ? employee.getId() : employee2.getId()))
                .forEach(it -> context.insertInto(APPROVALS).set(it).execute());

        var actual = provider.countOfEmployee(employee.getId());

        assertThat(actual).isEqualTo(50);
    }
}