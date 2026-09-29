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
import ru.sber.transport.fraud.monitoring.model.Fraud;
import ru.sber.transport.fraud.monitoring.providers.fraud.FraudsDatabaseDatabaseProviderImpl;
import ru.sber.transport.fraud.monitoring.providers.model.TestFraud;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка провайдера информации о фроде")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class FraudsDatabaseProviderImplTest {

    @Autowired
    private DSLContext context;

    private final FraudsDatabaseProvider fraudsDatabaseProvider = new FraudsDatabaseDatabaseProviderImpl() {

        @Override
        public DSLContext context() {
            return context;
        }
    };

    @Test
    @DisplayName("Проверка сохранения информации о фроде")
    void test_save() {
        final var source = new TestFraud(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class)
        );

        fraudsDatabaseProvider.save(source);

        assertThat(context.fetchCount(Tables.FRAUD)).isEqualTo(1);

        final var actual = context.selectFrom(Tables.FRAUD).fetchSingle();
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(source.getId());
        assertThat(actual.getRequestId()).isEqualTo(source.getRequestId());
        assertThat(actual.getComment()).isEqualTo(source.getComment());
        assertThat(actual.getFraudType()).isEqualTo(source.getFraudType());
        assertThat(actual.getSource()).isEqualTo(source.getSource());
    }

    @Test
    @DisplayName("Проверка поиска нарушений по requestId: найден один фрод")
    void test_findByRequestId_whenFraudExists_shouldReturnFraud() {
        final var requestId = UUID.randomUUID();
        final var fraud = new TestFraud(
                UUID.randomUUID(),
                requestId,
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class)
        );

        fraudsDatabaseProvider.save(fraud);

        final var result = fraudsDatabaseProvider.findByRequestId(requestId);

        assertThat(result).hasSize(1);
        final var actual = result.getFirst();
        assertThat(actual.getId()).isEqualTo(fraud.getId());
        assertThat(actual.getRequestId()).isEqualTo(fraud.getRequestId());
        assertThat(actual.getComment()).isEqualTo(fraud.getComment());
    }

    @Test
    @DisplayName("Проверка поиска нарушений по requestId: найдено несколько фродов")
    void test_findByRequestId_whenMultipleFraudsExist_shouldReturnAll() {
        final var requestId = UUID.randomUUID();
        final var fraud1 = new TestFraud(UUID.randomUUID(), requestId, Instancio.create(String.class),
                Instancio.create(String.class), Instancio.create(String.class));
        final var fraud2 = new TestFraud(UUID.randomUUID(), requestId, Instancio.create(String.class),
                Instancio.create(String.class), Instancio.create(String.class));

        fraudsDatabaseProvider.save(fraud1);
        fraudsDatabaseProvider.save(fraud2);

        final var result = fraudsDatabaseProvider.findByRequestId(requestId);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Fraud::getId)
                .containsExactlyInAnyOrder(fraud1.getId(), fraud2.getId());
    }

    @Test
    @DisplayName("Проверка поиска нарушений по requestId: фрод не найден")
    void test_findByRequestId_whenFraudNotExists_shouldReturnEmptyList() {
        final var requestId = UUID.randomUUID();

        final var result = fraudsDatabaseProvider.findByRequestId(requestId);

        assertThat(result).isEmpty();
    }

}