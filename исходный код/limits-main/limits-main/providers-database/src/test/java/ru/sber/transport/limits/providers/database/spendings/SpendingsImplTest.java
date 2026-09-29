package ru.sber.transport.limits.providers.database.spendings;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
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
import ru.sber.transport.database.limits.enums.SpendingStatus;
import ru.sber.transport.database.limits.tables.records.SpendingsRecord;
import ru.sber.transport.limits.model.PeriodSharing;
import ru.sber.transport.limits.model.Reserve;
import ru.sber.transport.limits.model.ReserveStatus;
import ru.sber.transport.limits.model.Spending;
import ru.sber.transport.limits.providers.Spendings;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static ru.sber.transport.database.limits.Tables.SPENDINGS;

@JooqTest
@Transactional
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
@ContextConfiguration(classes = {JooqDatabaseConfig.class, SpendingsImpl.class})
@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка провайдера расходов")
class SpendingsImplTest {

    @Autowired
    private DSLContext context;

    @Autowired
    private Spendings spendings;

    @Test
    @DisplayName("Проверка создания расхода")
    void test_create() {
        final var sharing = Instancio.create(TestPeriodSharing.class);
        final var data = Instancio.create(TestReserve.class);

        spendings.create(sharing, data);

        assertThat(context.fetchCount(SPENDINGS)).isEqualTo(1);

        final var actual = context.fetchOne(SPENDINGS);

        assertThat(actual).isNotNull();
        assertSoftly(it -> {
            it.assertThat(actual.getId()).isNotNull();
            it.assertThat(actual.getReserved()).isEqualTo(data.cost());
            it.assertThat(actual.getRequestId()).isEqualTo(data.id());
            it.assertThat(actual.getEmployeeId()).isEqualTo(data.consumerId());
            it.assertThat(actual.getReservationTime()).isNotNull();
            it.assertThat(actual.getPeriodSharingId()).isEqualTo(sharing.id());
        });

        final var data2 = Instancio.of(TestReserve.class)
                .set(Select.field(TestReserve::id), data.id())
                .create();

        spendings.create(sharing, data2);

        assertThat(context.fetchCount(SPENDINGS)).isEqualTo(1);

        final var actual2 = context.fetchOne(SPENDINGS);

        assertThat(actual2).isNotNull();
        assertSoftly(it -> {
            it.assertThat(actual2.getId()).isEqualTo(actual.getId());
            it.assertThat(actual2.getReserved()).isEqualTo(data2.cost());
            it.assertThat(actual2.getRequestId()).isEqualTo(data.id()).isEqualTo(data2.id());
            it.assertThat(actual2.getEmployeeId()).isEqualTo(data.consumerId());
            it.assertThat(actual2.getReservationTime()).isNotNull();
            it.assertThat(actual2.getPeriodSharingId()).isEqualTo(sharing.id());
        });
    }

    @Test
    @DisplayName("Проверка получения расхода по идентификатору")
    void test_get() {
        final var data = context.insertInto(SPENDINGS)
                .set(SPENDINGS.ID, UUID.randomUUID())
                .set(SPENDINGS.REQUEST_ID, UUID.randomUUID())
                .set(SPENDINGS.PERIOD_SHARING_ID, UUID.randomUUID())
                .set(SPENDINGS.RESERVED, Instancio.create(BigDecimal.class))
                .set(SPENDINGS.EMPLOYEE_ID, UUID.randomUUID())
                .set(SPENDINGS.RESERVATION_TIME, OffsetDateTime.now())
                .set(SPENDINGS.STATUS, Instancio.create(SpendingStatus.class))
                .returning()
                .fetchSingle();

        final var actualOpt = spendings.get(data.getRequestId());

        assertThat(actualOpt).isPresent();
        final var actual = actualOpt.get();
        assertSoftly(it -> {
            it.assertThat(actual.id()).isNotNull();
            it.assertThat(actual.reserved()).isEqualTo(data.getReserved());
            it.assertThat(actual.periodSharing()).isEqualTo(data.getPeriodSharingId());
        });
    }

    @Test
    @DisplayName("Проверка фиксирования трат")
    void test_update() {
        final var data = context.insertInto(SPENDINGS)
                .set(SPENDINGS.ID, UUID.randomUUID())
                .set(SPENDINGS.REQUEST_ID, UUID.randomUUID())
                .set(SPENDINGS.PERIOD_SHARING_ID, UUID.randomUUID())
                .set(SPENDINGS.RESERVED, Instancio.create(BigDecimal.class))
                .set(SPENDINGS.EMPLOYEE_ID, UUID.randomUUID())
                .set(SPENDINGS.RESERVATION_TIME, OffsetDateTime.now())
                .set(SPENDINGS.STATUS, SpendingStatus.RESERVED)
                .returning()
                .fetchSingle();

        final var newStatus = ReserveStatus.SPENT;

        final var reserve = new TestSpending(data);

        spendings.update(reserve, newStatus);

        final var actual = context.fetchSingle(SPENDINGS);

        assertSoftly(it -> {
            it.assertThat(actual.getStatus()).isEqualTo(SpendingStatus.SPENT);
            it.assertThat(actual.getSpendingTime()).isNotNull();
        });
    }

    @Test
    @DisplayName("Проверка отмены трат")
    void test_cancel() {
        final var data = context.insertInto(SPENDINGS)
                .set(SPENDINGS.ID, UUID.randomUUID())
                .set(SPENDINGS.REQUEST_ID, UUID.randomUUID())
                .set(SPENDINGS.PERIOD_SHARING_ID, UUID.randomUUID())
                .set(SPENDINGS.RESERVED, Instancio.create(BigDecimal.class))
                .set(SPENDINGS.EMPLOYEE_ID, UUID.randomUUID())
                .set(SPENDINGS.RESERVATION_TIME, OffsetDateTime.now())
                .set(SPENDINGS.STATUS, SpendingStatus.RESERVED)
                .returning()
                .fetchSingle();

        final var newStatus = ReserveStatus.CANCELED;

        final var reserve = new TestSpending(data);

        spendings.update(reserve, newStatus);

        final var actual = context.fetchSingle(SPENDINGS);

        assertSoftly(it -> {
            it.assertThat(actual.getStatus()).isEqualTo(SpendingStatus.CANCELED);
            it.assertThat(actual.getCancelTime()).isNotNull();
        });
    }

    private record TestPeriodSharing(UUID id, BigDecimal remains) implements PeriodSharing {
    }

    private record TestReserve(BigDecimal cost, UUID id, String service, String type, UUID consumerId,
                               OffsetDateTime date) implements Reserve {
    }

    private record TestSpending(UUID id, BigDecimal reserved, UUID periodSharing, ReserveStatus status) implements Spending {

        TestSpending(SpendingsRecord spendingsRecord) {
            this(spendingsRecord.getId(), spendingsRecord.getReserved(), spendingsRecord.getPeriodSharingId(), ReserveStatus.valueOf(spendingsRecord.getStatus().name()));
        }

    }

}