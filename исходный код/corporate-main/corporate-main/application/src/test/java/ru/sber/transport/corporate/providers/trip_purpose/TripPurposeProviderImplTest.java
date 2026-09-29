package ru.sber.transport.corporate.providers.trip_purpose;

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
import ru.sber.transport.corporate.business.model.TripPurpose;
import ru.sber.transport.corporate.business.providers.TripPurposeProvider;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sber.transport.database.corporate.Tables.TRIP_PURPOSE;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка провайдера целей поездок")
@JooqTest(properties = "logging.level.org.jooq.tools.LoggerListener=DEBUG")
@ContextConfiguration(classes = {JooqDatabaseConfig.class, TripPurposeProviderImpl.class, TripPurposeDatabaseMapperImpl.class})
@ActiveProfiles("test")
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
class TripPurposeProviderImplTest {

    @Autowired
    private DSLContext context;

    @Autowired
    private TripPurposeProvider provider;

    @Test
    @DisplayName("Получение цели поездки")
    void test_get() {
        var saved = context.insertInto(TRIP_PURPOSE)
                .set(TRIP_PURPOSE.ID, UUID.randomUUID())
                .set(TRIP_PURPOSE.LABEL, Instancio.create(String.class))
                .returning().fetchSingle();

        final var actualOpt = provider.get(saved.getId());

        assertThat(actualOpt).isPresent();

        final var actual = actualOpt.get();

        var expected = new TripPurpose();
        expected.setId(saved.getId());
        expected.setLabel(saved.getLabel());

        assertThat(actual).isEqualTo(expected);
        assertThat(actual).hasSameHashCodeAs(expected);
        assertThat(expected).isEqualTo(actual);

        var differentId = new TripPurpose();
        differentId.setId(UUID.randomUUID());
        differentId.setLabel(saved.getLabel());

        assertThat(actual).isNotEqualTo(differentId);
        assertThat(actual.hashCode()).isNotEqualTo(differentId.hashCode());

        var nullId = new TripPurpose();
        nullId.setId(null);
        nullId.setLabel(saved.getLabel());

        assertThat(actual).isNotEqualTo(nullId);
        assertThat(nullId).isNotEqualTo(actual);

        assertThat(actual).isNotEqualTo(null);
        assertThat(actual).isNotEqualTo("some string");
    }
}