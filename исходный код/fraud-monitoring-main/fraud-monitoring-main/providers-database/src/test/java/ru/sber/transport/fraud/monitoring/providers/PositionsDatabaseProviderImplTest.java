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
import ru.sber.transport.fraud.monitoring.providers.model.TestPosition;
import ru.sber.transport.fraud.monitoring.providers.position.PositionsDatabaseProviderImpl;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка провайдера должностей")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class PositionsDatabaseProviderImplTest {

    @Autowired
    private DSLContext context;

    private final PositionsDatabaseProvider positionsDatabaseProvider = new PositionsDatabaseProviderImpl() {

        @Override
        public DSLContext context() {
            return context;
        }
    };

    @Test
    @DisplayName("Проверка сохранения должности")
    void test_createOrUpdate() {
        final var source = new TestPosition(
                UUID.randomUUID(),
                Instancio.create(String.class)
        );

        positionsDatabaseProvider.createOrUpdate(source);

        assertThat(context.fetchCount(Tables.POSITION)).isEqualTo(1);

        final var actual = context.selectFrom(Tables.POSITION).fetchSingle();
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(source.getId());
        assertThat(actual.getName()).isEqualTo(source.getName());
    }

    @Test
    @DisplayName("Проверка получения должности")
    void test_get() {
        final var id = UUID.randomUUID();
        context.insertInto(Tables.POSITION)
                .set(Tables.POSITION.ID, id)
                .set(Tables.POSITION.NAME, "Name")
                .execute();

        final var actual = positionsDatabaseProvider.get(id);
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getName()).isEqualTo("Name");
    }

}