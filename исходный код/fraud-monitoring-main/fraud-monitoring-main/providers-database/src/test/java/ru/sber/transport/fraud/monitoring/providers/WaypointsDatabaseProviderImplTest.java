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
import ru.sber.transport.fraud.monitoring.providers.model.TestWaypoint;
import ru.sber.transport.fraud.monitoring.providers.waypoint.WaypointsDatabaseDatabaseProviderImpl;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.instancio.Select.field;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка провайдера путевых точек")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class, WaypointsDatabaseDatabaseProviderImpl.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class WaypointsDatabaseProviderImplTest {

    @Autowired
    private DSLContext context;

    @Autowired
    private WaypointsDatabaseProvider waypointsDatabaseProvider;

    @Test
    @DisplayName("Проверка сохранения путевой точки")
    void test_save() {
        final var source = Instancio.of(TestWaypoint.class)
                .set(field(TestWaypoint::id), UUID.randomUUID())
                .set(field(TestWaypoint::tripRequestId), UUID.randomUUID())
                .set(field(TestWaypoint::orderingIndex), 1)
                .set(field(TestWaypoint::waitTime), 10L)
                .set(field(TestWaypoint::country), "Россия")
                .set(field(TestWaypoint::region), "Москва")
                .set(field(TestWaypoint::city), "Москва")
                .set(field(TestWaypoint::street), "Ленина")
                .set(field(TestWaypoint::house), "1")
                .set(field(TestWaypoint::structure), "1")
                .set(field(TestWaypoint::building), "1")
                .set(field(TestWaypoint::address), "Россия, Москва, Москва, Ленина, 1, 1, 1")
                .create();

        final var actual = waypointsDatabaseProvider.save(source);

        assertSoftly(it -> {
            it.assertThat(actual).isNotNull();
            it.assertThat(actual.getId()).isEqualTo(source.id());
            it.assertThat(actual.getTripRequestId()).isEqualTo(source.tripRequestId());
            it.assertThat(actual.getCountry()).isEqualTo(source.country());
            it.assertThat(actual.getRegion()).isEqualTo(source.region());
            it.assertThat(actual.getCity()).isEqualTo(source.city());
            it.assertThat(actual.getStreet()).isEqualTo(source.street());
            it.assertThat(actual.getHouse()).isEqualTo(source.house());
            it.assertThat(actual.getStructure()).isEqualTo(source.structure());
            it.assertThat(actual.getBuilding()).isEqualTo(source.building());
            it.assertThat(actual.getOrderingIndex()).isEqualTo(source.orderingIndex());
            it.assertThat(actual.getWaitTime()).isEqualTo(source.waitTime());
            it.assertThat(actual.getAddress()).isNotEmpty();
        });

        assertThat(context.fetchCount(Tables.WAYPOINT)).isEqualTo(1);
    }
}