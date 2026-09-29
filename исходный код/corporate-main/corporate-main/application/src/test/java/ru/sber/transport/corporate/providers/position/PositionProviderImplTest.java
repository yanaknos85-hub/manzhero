package ru.sber.transport.corporate.providers.position;

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
import ru.sber.transport.corporate.business.model.Position;
import ru.sber.transport.corporate.business.model.PositionFilter;
import ru.sber.transport.corporate.business.providers.HumanReadableProvider;
import ru.sber.transport.corporate.business.providers.Provider;
import ru.sber.transport.database.corporate.tables.records.OrganizationRecord;
import ru.sber.transport.corporate.providers.position.mappers.PositionDatabaseMapperImpl;
import ru.sber.transport.database.corporate.tables.records.PositionRecord;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.utils.DataCreator;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.when;
import static ru.sber.transport.database.corporate.Tables.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка провайдера должностей")
@JooqTest(properties = "logging.level.org.jooq.tools.LoggerListener=DEBUG")
@ContextConfiguration(classes = {JooqDatabaseConfig.class, PositionProviderImpl.class, PositionDatabaseMapperImpl.class})
@ActiveProfiles("test")
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
class PositionProviderImplTest implements DataCreator {

    @Autowired
    private DSLContext context;

    @Autowired
    private Provider<Position, PositionFilter> provider;

    @MockitoBean
    private HumanReadableProvider<Position> humanReadableProvider;

    private OrganizationRecord organization;

    @BeforeEach
    void setup() {
        organization = context.insertInto(ORGANIZATION).set(createOrganizationRecord(0)).returning().fetchSingle();
    }

    @Test
    @DisplayName("Проверка сохранения")
    void test_save() {
        var source = Instancio.of(Position.class)
                .set(Select.field(Position::getOrganizationId), organization.getId())
                .create();

        var saved = provider.save(source);

        assertThat(context.fetchCount(context.selectFrom(POSITION)))
                .isEqualTo(1);

        var actual = context.selectFrom(POSITION).fetchSingle();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(source.getId());
            it.assertThat(actual.getStatus().name()).isEqualTo(source.getStatus().name());
            it.assertThat(actual.getName()).isEqualTo(source.getName());
            it.assertThat(actual.getOrganizationId()).isEqualTo(source.getOrganizationId());
            it.assertThat(actual.getStatus().name()).isEqualTo(source.getStatus().name());
            it.assertThat(actual.getHumanreadableid()).isEqualTo(source.getHumanReadableId());
        });

        assertSoftly(it -> {
            it.assertThat(saved.getId()).isEqualTo(source.getId());
            it.assertThat(saved.getStatus().name()).isEqualTo(source.getStatus().name());
            it.assertThat(saved.getName()).isEqualTo(source.getName());
            it.assertThat(saved.getOrganizationId()).isEqualTo(source.getOrganizationId());
            it.assertThat(saved.getStatus().name()).isEqualTo(source.getStatus().name());
            it.assertThat(saved.getHumanReadableId()).isEqualTo(source.getHumanReadableId());
        });
    }

    @Test
    @DisplayName("Получение всех")
    void test_get_all() {
        var expected = IntStream.range(0, 100)
                .mapToObj(it -> createPositionRecord(it, organization.getId()))
                .map(it -> context.insertInto(POSITION).set(it).returning().fetchSingle())
                .toList();

        var actualList = provider.get();

        assertThat(actualList).hasSameSizeAs(expected);
    }

    @Test
    @DisplayName("Получение одной записи")
    void test_get() {
        var expected = IntStream.range(0, 100)
                .mapToObj(it -> createPositionRecord(it, organization.getId()))
                .map(it -> context.insertInto(POSITION).set(it).returning().fetchSingle())
                .toList();

        var actual = provider.get(expected.get(10).getId());

        assertThat(actual).isPresent();

        assertSoftly(it -> {
            it.assertThat(actual.get().getId()).isEqualTo(expected.get(10).getId());
            it.assertThat(actual.get().getStatus().name()).isEqualTo(expected.get(10).getStatus().name());
            it.assertThat(actual.get().getName()).isEqualTo(expected.get(10).getName());
            it.assertThat(actual.get().getOrganizationId()).isEqualTo(expected.get(10).getOrganizationId());
            it.assertThat(actual.get().getStatus().name()).isEqualTo(expected.get(10).getStatus().name());
            it.assertThat(actual.get().getHumanReadableId()).isEqualTo(expected.get(10).getHumanreadableid());
        });
    }

    @Test
    @DisplayName("Сохранение всех записей")
    void test_save_all() {
        var data = Instancio.ofList(Position.class)
                .ignore(Select.field(Position::getHumanReadableId))
                .set(Select.field(Position::getOrganizationId), organization.getId())
                .create();

        when(humanReadableProvider.getNext(organization.getId(), data.size())).thenReturn(IntStream.range(0, data.size()).boxed().map(String::valueOf).toList());

        provider.saveAll(data);

        var actual = context.selectFrom(POSITION).orderBy(POSITION.HUMANREADABLEID).fetchInto(PositionRecord.class);

        assertThat(actual).hasSameSizeAs(data);

        for (var i = 0; i < actual.size(); i++) {
            assertThat(actual.get(i).getHumanreadableid()).endsWith(String.valueOf(i));
        }
    }
}