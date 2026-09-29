package ru.sber.transport.limits.providers.limits.sharing;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.database.limits.tables.records.EmployeeRecord;
import ru.sber.transport.database.limits.tables.records.LimitRecord;
import ru.sber.transport.limits.business.model.LimitSharing;
import ru.sber.transport.limits.business.providers.LimitSharingPerPeriodProvider;
import ru.sber.transport.limits.business.providers.LimitSharingProvider;
import ru.sber.transport.limits.providers.DataCreator;
import ru.sber.transport.limits.providers.limits.sharing.mappers.LimitSharingDatabaseMapperImpl;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static ru.sber.transport.database.limits.Tables.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка провайдера распределений за период")
@JooqTest
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
@ContextConfiguration(classes = {JooqDatabaseConfig.class, LimitSharingProviderImpl.class, LimitSharingDatabaseMapperImpl.class})
@MockitoBean(types = LimitSharingPerPeriodProvider.class)
class LimitSharingProviderImplTest implements DataCreator {

    @Autowired
    private DSLContext context;

    @Autowired
    private LimitSharingProvider provider;

    @Autowired
    private DSLContext dslContext;

    private EmployeeRecord employee;

    private LimitRecord limit1;

    private LimitRecord limit2;

    @BeforeEach
    void setup() {
        dslContext.insertInto(SERVICES)
                .values("PASSENGER")
                .execute();
        dslContext.insertInto(TYPES)
                .values("TAXI")
                .execute();
        employee = context.insertInto(EMPLOYEE).set(createEmployee()).returning().fetchSingle();
        limit1 = context.insertInto(LIMIT).set(createLimit()).returning().fetchSingle();
        limit2 = context.insertInto(LIMIT).set(createLimit()).returning().fetchSingle();
    }

    @Test
    @DisplayName("Проверка сохранения")
    void test_save() {
        final var type = dslContext.insertInto(TYPES)
                .values(Instancio.create(String.class))
                .returning(TYPES.ID).fetchSingle(TYPES.ID);
        limit1.setServiceType(type);

        var saved = context.insertInto(SHARINGS).set(createLimitSharing(limit1, employee)).returning().fetchSingle();
        assertThat(context.fetchCount(context.selectFrom(SHARINGS))).isEqualTo(1);

        var limit = Instancio.of(LimitSharing.class)
                .set(Select.field(LimitSharing::getId), saved.getId())
                .set(Select.field(LimitSharing::getTransportType), type)
                .create();

        provider.save(limit);

        assertThat(context.fetchCount(context.selectFrom(SHARINGS))).isEqualTo(1);

        var actual = context.selectFrom(SHARINGS).fetchSingle();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(limit.getId());
            it.assertThat(actual.getRemains()).isEqualTo(limit.getRemains());
            it.assertThat(actual.getSum()).isEqualTo(limit.getSum());
        });
    }

    @Test
    @DisplayName("Проверка получения")
    void test_get() {

        dslContext.insertInto(TYPES)
                .values("BICYCLE")
                .execute();

        dslContext.insertInto(TYPES)
                .values("COURIER")
                .execute();
        final var type = dslContext.insertInto(TYPES)
                .values(Instancio.create(String.class))
                .returning(TYPES.ID).fetchSingle(TYPES.ID);
        limit1.setServiceType(type);

        var limitSharing1 = context.insertInto(SHARINGS).set(createLimitSharing(limit1, employee, "BICYCLE")).returning().fetchSingle();
        var limitSharing2 = context.insertInto(SHARINGS).set(createLimitSharing(limit1, employee, "COURIER")).returning().fetchSingle();
        context.insertInto(SHARINGS).set(createLimitSharing(limit2, employee)).returning().fetchSingle();

        var actualList = provider.getAll(limit1.getId());

        assertThat(actualList).hasSize(2);
        assertSoftly(it -> {
            it.assertThat(actualList.getFirst().getId()).isEqualTo(limitSharing1.getId());
            it.assertThat(actualList.getFirst().getRemains()).isEqualTo(limitSharing1.getRemains());
            it.assertThat(actualList.getFirst().getSum()).isEqualTo(limitSharing1.getSum());
        });
        assertSoftly(it -> {
            it.assertThat(actualList.get(1).getId()).isEqualTo(limitSharing2.getId());
            it.assertThat(actualList.get(1).getRemains()).isEqualTo(limitSharing2.getRemains());
            it.assertThat(actualList.get(1).getSum()).isEqualTo(limitSharing2.getSum());
        });
    }

}