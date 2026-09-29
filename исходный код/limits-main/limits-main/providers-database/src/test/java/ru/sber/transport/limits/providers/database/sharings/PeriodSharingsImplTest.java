package ru.sber.transport.limits.providers.database.sharings;

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
import ru.sber.transport.database.limits.enums.Period;
import ru.sber.transport.database.limits.enums.SharingType;
import ru.sber.transport.database.limits.enums.Status;
import ru.sber.transport.database.limits.enums.Type;
import ru.sber.transport.database.limits.tables.records.LimitSharingPerPeriodRecord;
import ru.sber.transport.database.limits.tables.records.ServicesRecord;
import ru.sber.transport.database.limits.tables.records.TypesRecord;
import ru.sber.transport.limits.model.PeriodSharing;
import ru.sber.transport.limits.model.Service;
import ru.sber.transport.limits.providers.Departments;
import ru.sber.transport.limits.providers.PeriodSharings;
import ru.sber.transport.limits.providers.database.TestDepartment;
import ru.sber.transport.limits.providers.database.TestEmployee;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.sber.transport.database.limits.Tables.*;

@JooqTest
@Transactional
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка провайдера распределений по периодам")
class PeriodSharingsImplTest {

    @Autowired
    DSLContext context;

    private final Departments departments = mock(Departments.class);

    private final PeriodSharings periodSharings = new PeriodSharingsImpl(departments) {
        @Override
        public DSLContext context() {
            return context;
        }
    };

    @Test
    @DisplayName("Получение распределения по периоду по идентификатору")
    void test_get() {
        final var employee = context.insertInto(EMPLOYEE)
                .set(EMPLOYEE.ID, Instancio.create(UUID.class))
                .returning().fetchSingle();

        final var periodSharing = context.insertInto(LIMIT_SHARING_PER_PERIOD)
                .set(LIMIT_SHARING_PER_PERIOD.ID, Instancio.create(UUID.class))
                .set(LIMIT_SHARING_PER_PERIOD.PERIOD, Instancio.create(Period.class))
                .set(LIMIT_SHARING_PER_PERIOD.AUTHOR_ID, employee.getId())
                .set(LIMIT_SHARING_PER_PERIOD.SUM, Instancio.create(BigDecimal.class))
                .set(LIMIT_SHARING_PER_PERIOD.BALANCE, Instancio.create(BigDecimal.class))
                .set(LIMIT_SHARING_PER_PERIOD.CREATION_TIME, Instancio.create(LocalDateTime.class))
                .returning().fetchSingle();

        final var actualOpt = periodSharings.get(periodSharing.getId());

        assertThat(actualOpt).isPresent();

        final var actual = actualOpt.get();

        assertSoftly(it -> {
            it.assertThat(actual.id()).isEqualTo(periodSharing.getId());
            it.assertThat(actual.remains()).isEqualTo(periodSharing.getBalance());
        });
    }

    @Test
    @DisplayName("Обновление распределения по периоду")
    void test_update() {
        final var employee = context.insertInto(EMPLOYEE)
                .set(EMPLOYEE.ID, Instancio.create(UUID.class))
                .returning().fetchSingle();

        final var periodSharing = context.insertInto(LIMIT_SHARING_PER_PERIOD)
                .set(LIMIT_SHARING_PER_PERIOD.ID, Instancio.create(UUID.class))
                .set(LIMIT_SHARING_PER_PERIOD.PERIOD, Instancio.create(Period.class))
                .set(LIMIT_SHARING_PER_PERIOD.AUTHOR_ID, employee.getId())
                .set(LIMIT_SHARING_PER_PERIOD.SUM, Instancio.create(BigDecimal.class))
                .set(LIMIT_SHARING_PER_PERIOD.BALANCE, Instancio.create(BigDecimal.class))
                .set(LIMIT_SHARING_PER_PERIOD.CREATION_TIME, Instancio.create(LocalDateTime.class))
                .returning().fetchSingle();

        final var remains = Instancio.create(BigDecimal.class);
        final var businessPeriodSharing = new TestPeriodSharing(periodSharing);

        periodSharings.update(businessPeriodSharing, remains);

        final var actual = context.fetchSingle(LIMIT_SHARING_PER_PERIOD);

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(periodSharing.getId());
            it.assertThat(actual.getBalance()).isEqualTo(remains);
        });
    }

    @Test
    @DisplayName("Поиск распределений по периоду по сотруднику")
    void test_findOfEmployee() {
        final var date = OffsetDateTime.now();

        final var employee = context.insertInto(EMPLOYEE)
                .set(EMPLOYEE.ID, Instancio.create(UUID.class))
                .returning().fetchSingle();

        final var service = context.insertInto(SERVICES)
                .set(SERVICES.ID, Instancio.create(String.class))
                .returning().fetchSingle();

        final var limit = context.insertInto(LIMIT)
                .set(LIMIT.ID, Instancio.create(UUID.class))
                .set(LIMIT.LIMIT_TYPE, Instancio.create(Type.class))
                .set(LIMIT.LIMIT_STATUS, Status.SHARED)
                .set(LIMIT.YEAR, date.getYear())
                .set(LIMIT.CREATION_TIME, Instancio.create(OffsetDateTime.class))
                .set(LIMIT.UPDATE_TIME, Instancio.create(OffsetDateTime.class))
                .set(LIMIT.EMPLOYEE_ID, employee.getId())
                .set(LIMIT.AUTHOR_ID, employee.getId())
                .set(LIMIT.HUMAN_READABLE_ID, Instancio.create(String.class))
                .set(LIMIT.HASH, Instancio.create(String.class))
                .set(LIMIT.LIMIT_SHARING_TYPE, Instancio.create(SharingType.class))
                .set(LIMIT.SERVICE_TYPE, service.getId())
                .set(LIMIT.SUM, Instancio.create(BigDecimal.class))
                .set(LIMIT.RESERVE, Instancio.create(BigDecimal.class))
                .set(LIMIT.FINAL_SHARING, Instancio.create(Boolean.class))
                .set(LIMIT.USE_MY_LIMIT, Instancio.create(Boolean.class))
                .returning().fetchSingle();

        final var type = context.insertInto(TYPES)
                .set(TYPES.ID, Instancio.create(String.class))
                .returning().fetchSingle();

        final var sharing = context.insertInto(SHARINGS)
                .set(SHARINGS.ID, Instancio.create(UUID.class))
                .set(SHARINGS.SUM, Instancio.create(BigDecimal.class))
                .set(SHARINGS.CREATION_TIME, Instancio.create(LocalDateTime.class))
                .set(SHARINGS.LIMIT_ID, limit.getId())
                .set(SHARINGS.AUTHOR_ID, employee.getId())
                .set(SHARINGS.REMAINS, Instancio.create(BigDecimal.class))
                .set(SHARINGS.TRANSPORT_TYPE, type.getId())
                .returning().fetchSingle();

        final var perPeriod = context.insertInto(LIMIT_SHARING_PER_PERIOD)
                .set(LIMIT_SHARING_PER_PERIOD.ID, Instancio.create(UUID.class))
                .set(LIMIT_SHARING_PER_PERIOD.PERIOD, Period.valueOf(date.getMonth().name()))
                .set(LIMIT_SHARING_PER_PERIOD.AUTHOR_ID, employee.getId())
                .set(LIMIT_SHARING_PER_PERIOD.SUM, Instancio.create(BigDecimal.class))
                .set(LIMIT_SHARING_PER_PERIOD.BALANCE, Instancio.create(BigDecimal.class))
                .set(LIMIT_SHARING_PER_PERIOD.LIMIT_SHARING_ID, sharing.getId())
                .set(LIMIT_SHARING_PER_PERIOD.CREATION_TIME, Instancio.create(LocalDateTime.class))
                .returning().fetchSingle();

        final var actualOpt = periodSharings.get(new TestService(service), new TestType(type), date, new TestEmployee(employee), true);

        assertThat(actualOpt).isPresent();

        final var actual = actualOpt.get();
        assertSoftly(it -> {
            it.assertThat(actual.id()).isEqualTo(perPeriod.getId());
            it.assertThat(actual.remains()).isEqualTo(perPeriod.getBalance());
        });
    }

    @Test
    @DisplayName("Поиск распределений по периоду по подразделению")
    void test_findOfDepartment() {
        final var date = OffsetDateTime.now();

        final var parentDepartment = context.insertInto(DEPARTMENT)
                .set(DEPARTMENT.ID, Instancio.create(UUID.class))
                .returning().fetchSingle();

        final var department = context.insertInto(DEPARTMENT)
                .set(DEPARTMENT.ID, Instancio.create(UUID.class))
                .set(DEPARTMENT.PARENT_ID, parentDepartment.getId())
                .returning().fetchSingle();

        final var employee = context.insertInto(EMPLOYEE)
                .set(EMPLOYEE.ID, Instancio.create(UUID.class))
                .set(EMPLOYEE.DEPARTMENT_ID, department.getId())
                .returning().fetchSingle();

        final var service = context.insertInto(SERVICES)
                .set(SERVICES.ID, Instancio.create(String.class))
                .returning().fetchSingle();

        final var limit = context.insertInto(LIMIT)
                .set(LIMIT.ID, Instancio.create(UUID.class))
                .set(LIMIT.LIMIT_TYPE, Instancio.create(Type.class))
                .set(LIMIT.LIMIT_STATUS, Status.SHARED)
                .set(LIMIT.YEAR, date.getYear())
                .set(LIMIT.CREATION_TIME, Instancio.create(OffsetDateTime.class))
                .set(LIMIT.UPDATE_TIME, Instancio.create(OffsetDateTime.class))
                .set(LIMIT.DEPARTMENT_ID, parentDepartment.getId())
                .set(LIMIT.AUTHOR_ID, employee.getId())
                .set(LIMIT.HUMAN_READABLE_ID, Instancio.create(String.class))
                .set(LIMIT.HASH, Instancio.create(String.class))
                .set(LIMIT.LIMIT_SHARING_TYPE, Instancio.create(SharingType.class))
                .set(LIMIT.SERVICE_TYPE, service.getId())
                .set(LIMIT.SUM, Instancio.create(BigDecimal.class))
                .set(LIMIT.RESERVE, Instancio.create(BigDecimal.class))
                .set(LIMIT.FINAL_SHARING, Instancio.create(Boolean.class))
                .set(LIMIT.USE_MY_LIMIT, Instancio.create(Boolean.class))
                .returning().fetchSingle();

        final var type = context.insertInto(TYPES)
                .set(TYPES.ID, Instancio.create(String.class))
                .returning().fetchSingle();

        final var sharing = context.insertInto(SHARINGS)
                .set(SHARINGS.ID, Instancio.create(UUID.class))
                .set(SHARINGS.SUM, Instancio.create(BigDecimal.class))
                .set(SHARINGS.CREATION_TIME, Instancio.create(LocalDateTime.class))
                .set(SHARINGS.LIMIT_ID, limit.getId())
                .set(SHARINGS.AUTHOR_ID, employee.getId())
                .set(SHARINGS.REMAINS, Instancio.create(BigDecimal.class))
                .set(SHARINGS.TRANSPORT_TYPE, type.getId())
                .returning().fetchSingle();

        final var perPeriod = context.insertInto(LIMIT_SHARING_PER_PERIOD)
                .set(LIMIT_SHARING_PER_PERIOD.ID, Instancio.create(UUID.class))
                .set(LIMIT_SHARING_PER_PERIOD.PERIOD, Period.valueOf(date.getMonth().name()))
                .set(LIMIT_SHARING_PER_PERIOD.AUTHOR_ID, employee.getId())
                .set(LIMIT_SHARING_PER_PERIOD.SUM, Instancio.create(BigDecimal.class))
                .set(LIMIT_SHARING_PER_PERIOD.BALANCE, Instancio.create(BigDecimal.class))
                .set(LIMIT_SHARING_PER_PERIOD.LIMIT_SHARING_ID, sharing.getId())
                .set(LIMIT_SHARING_PER_PERIOD.CREATION_TIME, Instancio.create(LocalDateTime.class))
                .returning().fetchSingle();

        when(departments.get(department.getId())).thenReturn(new TestDepartment(department));
        when(departments.get(department.getParentId())).thenReturn(new TestDepartment(parentDepartment));

        final var actualOpt = periodSharings.get(new TestService(service), new TestType(type), date, new TestEmployee(employee), false);

        assertThat(actualOpt).isPresent();

        final var actual = actualOpt.get();
        assertSoftly(it -> {
            it.assertThat(actual.id()).isEqualTo(perPeriod.getId());
            it.assertThat(actual.remains()).isEqualTo(perPeriod.getBalance());
        });
    }

    private record TestPeriodSharing(UUID id, BigDecimal remains) implements PeriodSharing {

        TestPeriodSharing(LimitSharingPerPeriodRecord periodSharing) {
            this(periodSharing.getId(), periodSharing.getBalance());
        }

    }

    private record TestService(String name) implements Service {

        TestService(ServicesRecord service) {
            this(service.getId());
        }

    }

    private record TestType(String name) implements ru.sber.transport.limits.model.Type {

        TestType(TypesRecord type) {
            this(type.getId());
        }

    }

}