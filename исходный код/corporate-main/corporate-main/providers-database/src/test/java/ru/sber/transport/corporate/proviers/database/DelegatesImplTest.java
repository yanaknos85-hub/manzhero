package ru.sber.transport.corporate.proviers.database;

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
import ru.sber.transport.corporate.providers.Delegates;
import ru.sber.transport.database.corporate.enums.ActiveStatus;
import ru.sber.transport.database.corporate.enums.StructureType;
import ru.sber.transport.database.corporate.tables.records.DelegateRecord;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static ru.sber.transport.database.corporate.Tables.*;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка провайдера подразделений")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class DelegatesImplTest {

    @Autowired
    DSLContext context;

    private final Delegates delegates = new DelegatesImpl() {

        @Override
        public DSLContext context() {
            return context;
        }

    };

    @Test
    @DisplayName("Проверка получения количества делегатов")
    void test_countDelegates() {
        final var organization = context.insertInto(ORGANIZATION)
                .set(ORGANIZATION.ID, UUID.randomUUID())
                .set(ORGANIZATION.OFFICIAL_NAME, Instancio.create(String.class))
                .set(ORGANIZATION.TIN, Instancio.create(String.class))
                .set(ORGANIZATION.MSRN, Instancio.create(String.class))
                .set(ORGANIZATION.ADDRESS, Instancio.create(String.class))
                .set(ORGANIZATION.DIGIT_ID, Instancio.create(Long.class))
                .set(ORGANIZATION.STATUS, Instancio.create(ActiveStatus.class))
                .returning().fetchSingle();
        final var department = context.insertInto(DEPARTMENT)
                .set(DEPARTMENT.ID, UUID.randomUUID())
                .set(DEPARTMENT.NAME, Instancio.create(String.class))
                .set(DEPARTMENT.HUMANREADABLEID, Instancio.create(String.class))
                .set(DEPARTMENT.CODE, Instancio.create(String.class))
                .set(DEPARTMENT.ORG_STRUCTURE_TYPE, Instancio.create(StructureType.class))
                .set(DEPARTMENT.STATUS, Instancio.create(ActiveStatus.class))
                .set(DEPARTMENT.ORGANIZATION_ID, organization.getId())
                .returning().fetchSingle();
        final var position = context.insertInto(POSITION)
                .set(POSITION.ID, UUID.randomUUID())
                .set(POSITION.NAME, Instancio.create(String.class))
                .set(POSITION.HUMANREADABLEID, Instancio.create(String.class))
                .set(POSITION.STATUS, Instancio.create(ActiveStatus.class))
                .set(POSITION.ORGANIZATION_ID, organization.getId())
                .set(POSITION.ORG_STRUCTURE_TYPE, Instancio.create(StructureType.class))
                .returning().fetchSingle();
        for (var i = 0; i < 100; i++) {
            final var employee = context.insertInto(EMPLOYEE)
                    .set(EMPLOYEE.ID, UUID.randomUUID())
                    .set(EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
                    .set(EMPLOYEE.LAST_NAME, Instancio.create(String.class))
                    .set(EMPLOYEE.EMAIL, Instancio.create(String.class))
                    .set(EMPLOYEE.HUMANREADABLEID, Instancio.create(String.class))
                    .set(EMPLOYEE.MOBILE_PHONE, Instancio.create(String.class))
                    .set(EMPLOYEE.PERSONNEL_NUMBER, Instancio.create(String.class))
                    .set(EMPLOYEE.CONSENT, Instancio.create(Boolean.class))
                    .set(EMPLOYEE.STATUS, Instancio.create(ActiveStatus.class))
                    .set(EMPLOYEE.POSITION_ID, position.getId())
                    .set(EMPLOYEE.DEPARTMENT_ID, department.getId())
                    .set(EMPLOYEE.ORG_STRUCTURE_TYPE, Instancio.create(StructureType.class))
                    .set(EMPLOYEE.ORGANIZATION_ID, organization.getId())
                    .returning().fetchSingle();
            context.insertInto(DELEGATE)
                    .set(DELEGATE.ID, UUID.randomUUID())
                    .set(DELEGATE.USER_ID, employee.getId())
                    .set(DELEGATE.SUPERVISOR_ID, employee.getId())
                    .set(DELEGATE.START_DATE, Instancio.create(LocalDate.class))
                    .set(DELEGATE.END_DATE, Instancio.create(LocalDate.class))
                    .set(DELEGATE.STATUS, i % 2 == 0 ? "ACTIVE" : "INACTIVE")
                    .execute();
        }

        assertThat(delegates.count()).isEqualTo(50);
    }

    @Test
    @DisplayName("Проверка получения делегатов")
    void test_getDelegates() {
        final var organization = context.insertInto(ORGANIZATION)
                .set(ORGANIZATION.ID, UUID.randomUUID())
                .set(ORGANIZATION.OFFICIAL_NAME, Instancio.create(String.class))
                .set(ORGANIZATION.TIN, Instancio.create(String.class))
                .set(ORGANIZATION.MSRN, Instancio.create(String.class))
                .set(ORGANIZATION.ADDRESS, Instancio.create(String.class))
                .set(ORGANIZATION.DIGIT_ID, Instancio.create(Long.class))
                .set(ORGANIZATION.STATUS, Instancio.create(ActiveStatus.class))
                .returning().fetchSingle();
        final var department = context.insertInto(DEPARTMENT)
                .set(DEPARTMENT.ID, UUID.randomUUID())
                .set(DEPARTMENT.NAME, Instancio.create(String.class))
                .set(DEPARTMENT.HUMANREADABLEID, Instancio.create(String.class))
                .set(DEPARTMENT.CODE, Instancio.create(String.class))
                .set(DEPARTMENT.ORG_STRUCTURE_TYPE, Instancio.create(StructureType.class))
                .set(DEPARTMENT.STATUS, Instancio.create(ActiveStatus.class))
                .set(DEPARTMENT.ORGANIZATION_ID, organization.getId())
                .returning().fetchSingle();
        final var position = context.insertInto(POSITION)
                .set(POSITION.ID, UUID.randomUUID())
                .set(POSITION.NAME, Instancio.create(String.class))
                .set(POSITION.HUMANREADABLEID, Instancio.create(String.class))
                .set(POSITION.STATUS, Instancio.create(ActiveStatus.class))
                .set(POSITION.ORGANIZATION_ID, organization.getId())
                .set(POSITION.ORG_STRUCTURE_TYPE, Instancio.create(StructureType.class))
                .returning().fetchSingle();
        for (var i = 0; i < 100; i++) {
            final var employee = context.insertInto(EMPLOYEE)
                    .set(EMPLOYEE.ID, UUID.randomUUID())
                    .set(EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
                    .set(EMPLOYEE.LAST_NAME, Instancio.create(String.class))
                    .set(EMPLOYEE.EMAIL, Instancio.create(String.class))
                    .set(EMPLOYEE.HUMANREADABLEID, Instancio.create(String.class))
                    .set(EMPLOYEE.MOBILE_PHONE, Instancio.create(String.class))
                    .set(EMPLOYEE.PERSONNEL_NUMBER, Instancio.create(String.class))
                    .set(EMPLOYEE.CONSENT, Instancio.create(Boolean.class))
                    .set(EMPLOYEE.STATUS, Instancio.create(ActiveStatus.class))
                    .set(EMPLOYEE.POSITION_ID, position.getId())
                    .set(EMPLOYEE.DEPARTMENT_ID, department.getId())
                    .set(EMPLOYEE.ORG_STRUCTURE_TYPE, Instancio.create(StructureType.class))
                    .set(EMPLOYEE.ORGANIZATION_ID, organization.getId())
                    .returning().fetchSingle();
            context.insertInto(DELEGATE)
                    .set(DELEGATE.ID, UUID.randomUUID())
                    .set(DELEGATE.USER_ID, employee.getId())
                    .set(DELEGATE.SUPERVISOR_ID, employee.getId())
                    .set(DELEGATE.START_DATE, Instancio.create(LocalDate.class))
                    .set(DELEGATE.END_DATE, Instancio.create(LocalDate.class))
                    .set(DELEGATE.STATUS, i % 2 == 0 ? "INACTIVE" : "ACTIVE")
                    .execute();
        }

        final var actualList = delegates.get(10, 10);
        final var expectedList = context.selectFrom(DELEGATE).where(DELEGATE.STATUS.eq("ACTIVE")).orderBy(DELEGATE.START_DATE).limit(10).offset(10).fetchInto(DelegateRecord.class);

        assertThat(actualList).hasSameSizeAs(expectedList);

        final var size = actualList.size();
        for (var i = 0; i < size; i++) {
            final var actual = actualList.get(i);
            final var expected = expectedList.get(i);

            assertSoftly(it -> {
                assertThat(actual.id()).isEqualTo(expected.getId());
                assertThat(actual.supervisorId()).isEqualTo(expected.getSupervisorId());
                assertThat(actual.delegateId()).isEqualTo(expected.getUserId());
                assertThat(actual.startDate()).isEqualTo(expected.getStartDate());
                assertThat(actual.endDate()).isEqualTo(expected.getEndDate());
            });

        }
    }

}
