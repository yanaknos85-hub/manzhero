package ru.sber.transport.limits.providers.limits.sharing.period;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
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
import ru.sber.transport.database.limits.tables.records.EmployeeRecord;
import ru.sber.transport.database.limits.tables.records.LimitRecord;
import ru.sber.transport.database.limits.tables.records.SharingsRecord;
import ru.sber.transport.limits.business.model.LimitSharingPerPeriod;
import ru.sber.transport.limits.business.providers.LimitSharingPerPeriodProvider;
import ru.sber.transport.limits.providers.DataCreator;
import ru.sber.transport.limits.providers.limits.sharing.period.mappers.LimitSharingPerPeriodDatabaseMapperImpl;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static ru.sber.transport.database.limits.Tables.*;
import static ru.sber.transport.database.limits.tables.Limit.LIMIT;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка провайдера распределений за период")
@JooqTest
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
@ContextConfiguration(classes = {JooqDatabaseConfig.class, LimitSharingPerPeriodProviderImpl.class, LimitSharingPerPeriodDatabaseMapperImpl.class})
class LimitSharingPerPeriodProviderImplTest implements DataCreator {

    @Autowired
    private DSLContext context;

    @Autowired
    private LimitSharingPerPeriodProvider provider;

    private EmployeeRecord employee;

    private SharingsRecord limitSharing;

    private SharingsRecord limitSharing2;

    private LimitRecord limit;

    @BeforeEach
    void setup() {
        context.insertInto(SERVICES)
                .values("PASSENGER")
                .execute();
        context.insertInto(TYPES)
                .values("TAXI")
                .execute();
        context.insertInto(TYPES)
                .values("BICYCLE")
                .execute();
        context.insertInto(SERVICES)
                .values("CARGO")
                .execute();
        employee = context.insertInto(EMPLOYEE).set(createEmployee()).returning().fetchSingle();
        limit = context.insertInto(LIMIT).set(createLimit()).returning().fetchSingle();
        limitSharing = context.insertInto(SHARINGS).set(createLimitSharing(limit, employee, "BICYCLE")).returning().fetchSingle();
        limitSharing2 = context.insertInto(SHARINGS).set(createLimitSharing(limit, employee, "TAXI")).returning().fetchSingle();
    }

    @Test
    @DisplayName("Проверка сохранения")
    void test_save() {
        var saved = context.insertInto(LIMIT_SHARING_PER_PERIOD).set(createLimitSharingPerPeriod(limitSharing, employee)).returning().fetchSingle();
        assertThat(context.fetchCount(context.selectFrom(LIMIT_SHARING_PER_PERIOD))).isEqualTo(1);
        var limit = Instancio.of(LimitSharingPerPeriod.class)
                .set(Select.field(LimitSharingPerPeriod::getId), saved.getId())
                .set(Select.field(LimitSharingPerPeriod::getLimitSharingId), saved.getLimitSharingId())
                .set(Select.field(LimitSharingPerPeriod::getAuthorId), employee.getId())
                .create();

        provider.save(limit);

        assertThat(context.fetchCount(context.selectFrom(LIMIT_SHARING_PER_PERIOD))).isEqualTo(1);

        var actual = context.selectFrom(LIMIT_SHARING_PER_PERIOD).fetchSingle();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(limit.getId());
            it.assertThat(actual.getBalance()).isEqualTo(limit.getBalance());
            it.assertThat(actual.getSum()).isEqualTo(limit.getSum());
        });
    }

    @Test
    @DisplayName("Проверка получения")
    void test_get() {
        var limitSharingPerPeriodRecord1 = context.insertInto(LIMIT_SHARING_PER_PERIOD).set(createLimitSharingPerPeriod(limitSharing, employee, Period.JANUARY)).returning().fetchSingle();
        var limitSharingPerPeriodRecord2 = context.insertInto(LIMIT_SHARING_PER_PERIOD).set(createLimitSharingPerPeriod(limitSharing, employee, Period.FEBRUARY)).returning().fetchSingle();
        context.insertInto(LIMIT_SHARING_PER_PERIOD).set(createLimitSharingPerPeriod(limitSharing2, employee)).returning().fetchSingle();

        var actualList = provider.getAll(limitSharing.getId());

        assertThat(actualList).hasSize(2);
        assertSoftly(it -> {
            it.assertThat(actualList.getFirst().getId()).isEqualTo(limitSharingPerPeriodRecord1.getId());
            it.assertThat(actualList.getFirst().getBalance()).isEqualTo(limitSharingPerPeriodRecord1.getBalance());
            it.assertThat(actualList.getFirst().getSum()).isEqualTo(limitSharingPerPeriodRecord1.getSum());
        });
        assertSoftly(it -> {
            it.assertThat(actualList.get(1).getId()).isEqualTo(limitSharingPerPeriodRecord2.getId());
            it.assertThat(actualList.get(1).getBalance()).isEqualTo(limitSharingPerPeriodRecord2.getBalance());
            it.assertThat(actualList.get(1).getSum()).isEqualTo(limitSharingPerPeriodRecord2.getSum());
        });
    }

    @Test
    @DisplayName("Проверка получения не перемещенного лимита")
    void test_getNotMoved() {
        context.insertInto(LIMIT_SHARING_PER_PERIOD).set(createLimitSharingPerPeriod(limitSharing, employee, Period.JANUARY, true)).execute();
        context.insertInto(LIMIT_SHARING_PER_PERIOD).set(createLimitSharingPerPeriod(limitSharing2, employee)).execute();

        var actualList = provider.getNotMoved(LocalDate.of(2024, 1, 1));

        assertThat(actualList).hasSize(0);
    }

    @Test
    @DisplayName("Пометка лимита как уведомленного")
    void test_markAsNotified() {
        var limitSharingPerPeriod = context.insertInto(LIMIT_SHARING_PER_PERIOD).set(createLimitSharingPerPeriod(limitSharing, employee)).returning().fetchSingle();

        provider.setNotified(limitSharingPerPeriod.getId());

        var actual = context.selectFrom(LIMIT_SHARING_PER_PERIOD).fetchSingle();

        assertThat(actual.getNoticedAt()).isNotNull();
    }


}