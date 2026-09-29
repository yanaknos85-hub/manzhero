package ru.sber.transport.fraud.monitoring.providers;

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
import ru.sber.transport.database.fraud_monitoring.Tables;
import ru.sber.transport.fraud.monitoring.providers.model.TestTripPurpose;
import ru.sber.transport.fraud.monitoring.providers.trip_purpose.TripPurposesDatabaseProviderImpl;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка провайдера целей поездки")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class TripPurposesDatabaseProviderImplTest {

    @Autowired
    private DSLContext context;

    private final TripPurposesDatabaseProvider tripPurposesProvider = new TripPurposesDatabaseProviderImpl() {

        @Override
        public DSLContext context() {
            return context;
        }
    };

    @Test
    @DisplayName("Проверка сохранения цели поездки")
    void test_createOrUpdate() {
        final var source = new TestTripPurpose(
                UUID.randomUUID(),
                Instancio.create(String.class)
        );

        tripPurposesProvider.createOrUpdate(source);

        assertThat(context.fetchCount(Tables.TRIP_PURPOSE)).isEqualTo(1);

        final var actual = context.selectFrom(Tables.TRIP_PURPOSE).fetchSingle();
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(source.getId());
        assertThat(actual.getLabel()).isEqualTo(source.getLabel());
    }

    @Test
    @DisplayName("Проверка получения цели поездки")
    void test_get() {
        final var id = UUID.randomUUID();
        context.insertInto(Tables.TRIP_PURPOSE)
                .set(Tables.TRIP_PURPOSE.ID, id)
                .set(Tables.TRIP_PURPOSE.LABEL, "Name")
                .execute();

        final var actual = tripPurposesProvider.get(id);
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getLabel()).isEqualTo("Name");
    }
}