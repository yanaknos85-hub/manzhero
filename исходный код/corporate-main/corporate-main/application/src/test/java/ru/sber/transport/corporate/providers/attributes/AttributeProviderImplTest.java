package ru.sber.transport.corporate.providers.attributes;

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
import ru.sber.transport.corporate.business.providers.AttributeProvider;
import ru.sber.transport.database.corporate.tables.records.EmployeeAttributeRecord;
import ru.sber.transport.corporate.providers.attributes.mappers.AttributeDatabaseMapperImpl;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.utils.DataCreator;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static ru.sber.transport.database.corporate.Tables.*;
import static ru.sber.transport.database.corporate.tables.Organization.ORGANIZATION;
import static ru.sber.transport.database.corporate.tables.Position.POSITION;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка провайдера атрибутов")
@JooqTest
@ContextConfiguration(classes = {JooqDatabaseConfig.class, AttributeProviderImpl.class, AttributeDatabaseMapperImpl.class})
@ActiveProfiles("test")
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
class AttributeProviderImplTest implements DataCreator {

    @Autowired
    private DSLContext context;

    @Autowired
    private AttributeProvider provider;

    @Test
    @DisplayName("Получение атрибутов пользователя")
    void test_find_by_employee() {
        var organization = context.insertInto(ORGANIZATION).set(createOrganizationRecord(0)).returning().fetchSingle();
        var position = context.insertInto(POSITION).set(createPositionRecord(0, organization.getId())).returning().fetchSingle();
        var department = context.insertInto(DEPARTMENT).set(createDepartmentRecord(0, organization.getId())).returning().fetchSingle();
        var employee = context.insertInto(EMPLOYEE).set(createEmployeeRecord(0, organization.getId(), department.getId(), position.getId())).returning().fetchSingle();

        var attributes = IntStream.range(0, 100)
                .mapToObj(this::createAttributeRecord)
                .map(it -> context.insertInto(ATTRIBUTE).set(it).returning().fetchSingle())
                .toList();

        for (var i = 0; i < 100; i += 2) {
            context.insertInto(EMPLOYEE_ATTRIBUTE).set(new EmployeeAttributeRecord(employee.getId(), attributes.get(i).getId())).execute();
        }

        var actualList = provider.findOfEmployee(employee.getId());

        assertThat(actualList).hasSize(50);

        for (var i = 0; i < 50; i++) {
            var actual = actualList.get(i);
            var expected = attributes.get(i * 2);

            assertSoftly(it -> {
               it.assertThat(actual.getId()).isEqualTo(expected.getId());
               it.assertThat(actual.getName()).isEqualTo(expected.getName());
               it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
            });
        }
    }

}