package ru.sber.transport.limits.providers.limits;

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
import ru.sber.transport.database.limits.tables.LimitResponsibleEmployee;
import ru.sber.transport.database.limits.tables.records.LimitRecord;
import ru.sber.transport.limits.business.model.Limit;
import ru.sber.transport.limits.business.model.Status;
import ru.sber.transport.limits.business.providers.LimitsProvider;
import ru.sber.transport.limits.config.EnableApplicationConfig;
import ru.sber.transport.limits.providers.DataCreator;
import ru.sber.transport.limits.providers.limits.mappers.LimitsDatabaseMapperImpl;
import ru.sber.transport.limits.web.http.model.LimitWebFilter;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.request.Direction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static ru.sber.transport.database.limits.Tables.*;
import static ru.sber.transport.database.limits.tables.Limit.LIMIT;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка провайдера лимитов")
@JooqTest
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
@ContextConfiguration(classes = {JooqDatabaseConfig.class, LimitsProviderImpl.class, LimitsDatabaseMapperImpl.class})
@EnableApplicationConfig
class LimitsProviderImplTest implements DataCreator {

    @Autowired
    private DSLContext context;

    @Autowired
    private LimitsProvider provider;

    @BeforeEach
    void init() {
        context.insertInto(SERVICES)
                .values("PASSENGER")
                .execute();
        context.insertInto(SERVICES)
                .values("REPAIR")
                .execute();
        context.insertInto(TYPES)
                .values("TAXI")
                .execute();
        context.insertInto(SERVICES)
                .values("CARGO")
                .execute();
    }

    @Test
    @DisplayName("Проверка сохранения")
    void test_save() {
        var saved = context.insertInto(LIMIT).set(createLimit()).returning().fetchSingle();
        assertThat(context.fetchCount(context.selectFrom(LIMIT))).isEqualTo(1);
        var limit = Instancio.of(Limit.class)
                .set(Select.field(Limit::getId), saved.getId())
                .ignore(Select.field(Limit::getParentId))
                .set(Select.field(Limit::getServiceType), "PASSENGER")
                .create();

        provider.save(limit);

        assertThat(context.fetchCount(context.selectFrom(LIMIT))).isEqualTo(1);

        var actual = context.selectFrom(LIMIT).fetchSingle();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(limit.getId());
            it.assertThat(actual.getParentId()).isEqualTo(limit.getParentId());
            it.assertThat(actual.getLimitStatus().name()).isEqualTo(limit.getStatus().name());
            it.assertThat(actual.getSum()).isEqualTo(limit.getSum());
            it.assertThat(actual.getReserve()).isEqualTo(limit.getReserve());
        });
    }

    @Test
    @DisplayName("Проверка получения")
    void test_get() {
        var limit = context.insertInto(LIMIT).set(createLimit()).returning().fetchSingle();

        var actual = provider.get(limit.getId()).orElseThrow();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(limit.getId());
            it.assertThat(actual.getParentId()).isEqualTo(limit.getParentId());
            it.assertThat(actual.getStatus().name()).isEqualTo(limit.getLimitStatus().name());
            it.assertThat(actual.getSum()).isEqualTo(limit.getSum());
            it.assertThat(actual.getReserve()).isEqualTo(limit.getReserve());
        });
    }

    @Test
    @DisplayName("Проверка получения")
    void test_getChildren() {
        var limit = context.insertInto(LIMIT).set(createLimit()).returning().fetchSingle();
        var limitChild = context.insertInto(LIMIT).set(createLimit(limit)).returning().fetchSingle();

        var actualList = provider.getChildren(limit.getId());

        assertSoftly(it -> {
            it.assertThat(actualList.getFirst().getId()).isEqualTo(limit.getId());
            it.assertThat(actualList.getFirst().getParentId()).isEqualTo(limit.getParentId());
            it.assertThat(actualList.getFirst().getStatus().name()).isEqualTo(limit.getLimitStatus().name());
            it.assertThat(actualList.getFirst().getSum()).isEqualTo(limit.getSum());
            it.assertThat(actualList.getFirst().getReserve()).isEqualTo(limit.getReserve());
        });
        assertSoftly(it -> {
            it.assertThat(actualList.get(1).getId()).isEqualTo(limitChild.getId());
            it.assertThat(actualList.get(1).getParentId()).isEqualTo(limitChild.getParentId());
            it.assertThat(actualList.get(1).getStatus().name()).isEqualTo(limitChild.getLimitStatus().name());
            it.assertThat(actualList.get(1).getSum()).isEqualTo(limitChild.getSum());
            it.assertThat(actualList.get(1).getReserve()).isEqualTo(limitChild.getReserve());
        });
    }

    @Test
    @DisplayName("Проверка получения верхнеуровневого")
    void test_upper() {
        var limit = context.insertInto(LIMIT).set(createLimit()).returning().fetchSingle();
        var child = context.insertInto(LIMIT).set(createLimit(limit)).returning().fetchSingle();

        var actual = provider.upper(child.getId()).orElseThrow();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(limit.getId());
            it.assertThat(actual.getParentId()).isEqualTo(limit.getParentId());
            it.assertThat(actual.getStatus().name()).isEqualTo(limit.getLimitStatus().name());
            it.assertThat(actual.getSum()).isEqualTo(limit.getSum());
            it.assertThat(actual.getReserve()).isEqualTo(limit.getReserve());
        });
    }

    @Test
    @DisplayName("Проверка обновления")
    void test_update() {
        var limitRecord = context.insertInto(LIMIT).set(createLimit()).returning().fetchSingle();

        var employee1 = context.insertInto(EMPLOYEE)
                .set(createEmployee())
                .returning().fetchSingle();

        var employee2 = context.insertInto(EMPLOYEE)
                .set(createEmployee())
                .returning().fetchSingle();

        var employee3 = context.insertInto(EMPLOYEE)
                .set(createEmployee())
                .returning().fetchSingle();

        var employee4 = context.insertInto(EMPLOYEE)
                .set(createEmployee())
                .returning().fetchSingle();

        var employee5 = context.insertInto(EMPLOYEE)
                .set(createEmployee())
                .returning().fetchSingle();

        context.insertInto(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE)
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.LIMIT_ID, limitRecord.getId())
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.EMPLOYEE_ID, employee1.getId())
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.START_DATE, OffsetDateTime.now().minusDays(10))
                .execute();

        context.insertInto(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE)
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.LIMIT_ID, limitRecord.getId())
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.EMPLOYEE_ID, employee2.getId())
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.START_DATE, OffsetDateTime.now().minusDays(9))
                .returning().fetchSingle();

        context.insertInto(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE)
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.LIMIT_ID, limitRecord.getId())
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.EMPLOYEE_ID, employee3.getId())
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.START_DATE, OffsetDateTime.now().minusDays(8))
                .execute();

        context.insertInto(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE)
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.LIMIT_ID, limitRecord.getId())
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.EMPLOYEE_ID, employee4.getId())
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.START_DATE, OffsetDateTime.now().minusDays(7))
                .execute();

        var limit = new Limit();
        limit.setId(limitRecord.getId());

        var newData = Instancio.create(Limit.class);
        newData.setSum(BigDecimal.valueOf(4000));
        newData.setResponsibles(new ArrayList<>(List.of(employee5.getId())));

        var updateFields = new ArrayList<String>();
        updateFields.add("REPLACE:sum");
        updateFields.add("REPLACE:responsibles[3]");

        var updated = provider.update(limit, newData, updateFields);

        assertSoftly(it -> {
            it.assertThat(updated.getId()).isEqualTo(limitRecord.getId());
            it.assertThat(updated.getSum().stripTrailingZeros().toPlainString()).isEqualTo(newData.getSum().stripTrailingZeros().toPlainString());
            it.assertThat(updated.getResponsibles()).contains(newData.getResponsibles().getFirst());
        });
    }

    @Test
    @DisplayName("Проверка добавления")
    void test_add() {
        var limitRecord = context.insertInto(LIMIT).set(createLimit()).returning().fetchSingle();

        var employee1 = context.insertInto(EMPLOYEE)
                .set(createEmployee())
                .returning().fetchSingle();

        var employee2 = context.insertInto(EMPLOYEE)
                .set(createEmployee())
                .returning().fetchSingle();

        var employee3 = context.insertInto(EMPLOYEE)
                .set(createEmployee())
                .returning().fetchSingle();

        var employee4 = context.insertInto(EMPLOYEE)
                .set(createEmployee())
                .returning().fetchSingle();

        var employee5 = context.insertInto(EMPLOYEE)
                .set(createEmployee())
                .returning().fetchSingle();

        context.insertInto(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE)
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.LIMIT_ID, limitRecord.getId())
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.EMPLOYEE_ID, employee1.getId())
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.START_DATE, OffsetDateTime.now().minusDays(10))
                .execute();

        context.insertInto(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE)
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.LIMIT_ID, limitRecord.getId())
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.EMPLOYEE_ID, employee2.getId())
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.START_DATE, OffsetDateTime.now().minusDays(9))
                .returning().fetchSingle();

        context.insertInto(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE)
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.LIMIT_ID, limitRecord.getId())
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.EMPLOYEE_ID, employee3.getId())
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.START_DATE, OffsetDateTime.now().minusDays(8))
                .execute();

        context.insertInto(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE)
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.LIMIT_ID, limitRecord.getId())
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.EMPLOYEE_ID, employee4.getId())
                .set(LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE.START_DATE, OffsetDateTime.now().minusDays(7))
                .execute();

        var limit = new Limit();
        limit.setId(limitRecord.getId());

        var newData = Instancio.create(Limit.class);
        newData.setSum(BigDecimal.valueOf(4000));
        newData.setResponsibles(new ArrayList<>(List.of(employee5.getId())));

        var updateFields = new ArrayList<String>();
        updateFields.add("ADD:sum");
        updateFields.add("ADD:responsibles[3]");

        var updated = provider.update(limit, newData, updateFields);

        assertSoftly(it -> {
            it.assertThat(updated.getId()).isEqualTo(limitRecord.getId());
            it.assertThat(updated.getSum().stripTrailingZeros().toPlainString()).isEqualTo(limitRecord.getSum().add(newData.getSum()).stripTrailingZeros().toPlainString());
            it.assertThat(updated.getResponsibles().getLast()).isEqualTo(newData.getResponsibles().getFirst());
        });
    }

    @Test
    @DisplayName("Проверка получения хэша лимита")
    void test_hash() {
        var limit = createLimit();
        context.insertInto(LIMIT).set(limit).returning().fetchSingle();

        var actualOpt = provider.get(limit.getId(), OffsetDateTime.now());

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.orElseThrow();
        assertSoftly(it -> it.assertThat(actual.data().getHash()).isEqualTo(limit.getHash()));
    }

    @Test
    @DisplayName("Проверка получения изменений лимита. Изменено")
    void test_getModified_modified() {
        var updated = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        var lastRequested = OffsetDateTime.of(2024, 12, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        var limit = createLimit();
        limit.setUpdateTime(updated);
        context.insertInto(LIMIT).set(limit).returning().fetchSingle();

        var actualOpt = provider.get(limit.getId(), lastRequested);

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.orElseThrow();
        assertSoftly(it -> {
            it.assertThat(actual.data().getSum()).isEqualTo(limit.getSum());
            it.assertThat(actual.data().getId()).isEqualTo(limit.getId());
            it.assertThat(actual.data().getHash()).isEqualTo(limit.getHash());
            it.assertThat(actual.modified()).isEqualTo(true);
        });
    }

    @Test
    @DisplayName("Проверка получения изменений лимита. Не изменено")
    void test_getModified_not_modified() {
        var updated = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        var lastRequested = OffsetDateTime.of(2025, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        var limit = createLimit();
        limit.setUpdateTime(updated);
        context.insertInto(LIMIT).set(limit).returning().fetchSingle();

        var actualOpt = provider.get(limit.getId(), lastRequested);

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.orElseThrow();
        assertSoftly(it -> {
            it.assertThat(actual.data().getSum()).isNull();
            it.assertThat(actual.data().getId()).isNull();
            it.assertThat(actual.data().getHash()).isEqualTo(limit.getHash());
            it.assertThat(actual.modified()).isEqualTo(false);
        });
    }

    @Test
    @DisplayName("Проверка получения хэша лимита. Изменено")
    void test_hash_modified() {
        var limit = createLimit();
        limit.setUpdateTime(OffsetDateTime.now().plusDays(1));
        context.insertInto(LIMIT).set(limit).returning().fetchSingle();

        var actualOpt = provider.hash(limit.getId(), OffsetDateTime.now());

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.orElseThrow();
        assertSoftly(it -> {
            it.assertThat(actual.data().getHash()).isEqualTo(limit.getHash());
            it.assertThat(actual.modified()).isEqualTo(true);
        });
    }

    @Test
    @DisplayName("Проверка получения хэша лимита. Не изменено")
    void test_hash_not_modified() {
        var limit = createLimit();
        limit.setUpdateTime(OffsetDateTime.now().minusDays(1));
        context.insertInto(LIMIT).set(limit).returning().fetchSingle();

        var actualOpt = provider.hash(limit.getId(), OffsetDateTime.now());

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.orElseThrow();
        assertSoftly(it -> {
            it.assertThat(actual.data().getSum()).isNull();
            it.assertThat(actual.data().getId()).isNull();
            it.assertThat(actual.data().getHash()).isEqualTo(limit.getHash());
            it.assertThat(actual.modified()).isEqualTo(false);
        });
    }

    @Test
    @DisplayName("Проверка получения лимитов по организации")
    void test_get_organization() {
        var organization1 = UUID.randomUUID();
        var organization2 = UUID.randomUUID();
        var limits = new ArrayList<LimitRecord>();
        for (var i = 0; i < 100; i++) {
            limits.add(context.insertInto(LIMIT).set(createLimit(i % 2 == 0 ? organization1 : organization2, "HRI %03d".formatted(i))).returning().fetchSingle());
        }

        var actualPage = provider.get(organization1, LimitWebFilter.builder().build(), 0, 25, "human_readable_id", Direction.ASC);

        assertThat(actualPage.getContent()).hasSize(25);
        assertThat(actualPage.getPageData().totalElements()).isEqualTo(50);
        assertThat(actualPage.getPageData().totalPages()).isEqualTo(2);
        for (var i = 0; i < 25; i++) {
            var actual = actualPage.getContent().get(i);
            var expected = limits.get(i * 2);
            assertThat(actual.getId()).isEqualTo(expected.getId());
        }
    }

    @Test
    @DisplayName("Проверка получения лимитов по подразделению")
    void test_get_department() {
        var organization = UUID.randomUUID();
        var department = UUID.randomUUID();
        var department2 = UUID.randomUUID();
        context.insertInto(LIMIT).set(createLimit(organization, department, 2025, Status.PLANNING, "PASSENGER")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department, 2025, Status.SHARED, "PASSENGER")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department, 2025, Status.CLOSED, "PASSENGER")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department, 2025, Status.PLANNING, "CARGO")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department, 2025, Status.SHARED, "CARGO")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department, 2025, Status.CLOSED, "CARGO")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department, 2025, Status.PLANNING, "REPAIR")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department, 2025, Status.SHARED, "REPAIR")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department, 2025, Status.CLOSED, "REPAIR")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department, 2024, Status.PLANNING, "PASSENGER")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department, 2024, Status.SHARED, "PASSENGER")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department, 2024, Status.CLOSED, "PASSENGER")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department, 2024, Status.PLANNING, "CARGO")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department, 2024, Status.SHARED, "CARGO")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department, 2024, Status.CLOSED, "CARGO")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department, 2024, Status.PLANNING, "REPAIR")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department, 2024, Status.SHARED, "REPAIR")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department, 2024, Status.CLOSED, "REPAIR")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department2, 2025, Status.PLANNING, "PASSENGER")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department2, 2025, Status.SHARED, "PASSENGER")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department2, 2025, Status.CLOSED, "PASSENGER")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department2, 2025, Status.PLANNING, "CARGO")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department2, 2025, Status.SHARED, "CARGO")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department2, 2025, Status.CLOSED, "CARGO")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department2, 2025, Status.PLANNING, "REPAIR")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department2, 2025, Status.SHARED, "REPAIR")).returning().fetchSingle();
        context.insertInto(LIMIT).set(createLimit(organization, department2, 2025, Status.CLOSED, "REPAIR")).returning().fetchSingle();

        var actualPage = provider.get(organization, LimitWebFilter.builder().departmentId(department).build(), 0, 25, "human_readable_id", Direction.ASC);

        assertThat(actualPage.getContent()).hasSize(18);
        assertThat(actualPage.getPageData().totalElements()).isEqualTo(18);
        assertThat(actualPage.getPageData().totalPages()).isEqualTo(1);

        var actualPage2 = provider.get(organization, LimitWebFilter.builder().departmentId(department).year(2025).build(), 0, 25, "human_readable_id", Direction.ASC);

        assertThat(actualPage2.getContent()).hasSize(9);
        assertThat(actualPage2.getPageData().totalElements()).isEqualTo(9);
        assertThat(actualPage2.getPageData().totalPages()).isEqualTo(1);

        var actualPage3 = provider.get(organization, LimitWebFilter.builder().departmentId(department).year(2025).serviceType("PASSENGER").build(), 0, 25, "human_readable_id", Direction.ASC);

        assertThat(actualPage3.getContent()).hasSize(3);
        assertThat(actualPage3.getPageData().totalElements()).isEqualTo(3);
        assertThat(actualPage3.getPageData().totalPages()).isEqualTo(1);

        var actualPage4 = provider.get(organization, LimitWebFilter.builder().departmentId(department).year(2025).serviceType("PASSENGER").status(Status.SHARED).build(), 0, 25, "human_readable_id", Direction.ASC);

        assertThat(actualPage4.getContent()).hasSize(1);
        assertThat(actualPage4.getPageData().totalElements()).isEqualTo(1);
        assertThat(actualPage4.getPageData().totalPages()).isEqualTo(1);
    }

}