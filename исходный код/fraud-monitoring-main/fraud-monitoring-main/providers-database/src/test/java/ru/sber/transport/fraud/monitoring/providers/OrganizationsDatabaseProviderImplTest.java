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
import ru.sber.transport.fraud.monitoring.providers.model.TestOrganization;
import ru.sber.transport.fraud.monitoring.providers.organization.OrganizationsDatabaseProviderImpl;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка провайдера организаций")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class OrganizationsDatabaseProviderImplTest {

    @Autowired
    private DSLContext context;

    private final OrganizationsDatabaseProvider organizationsProvider = new OrganizationsDatabaseProviderImpl() {

        @Override
        public DSLContext context() {
            return context;
        }
    };

    @Test
    @DisplayName("Проверка сохранения организации")
    void test_createOrUpdate() {
        final var source = new TestOrganization(
                UUID.randomUUID(),
                Instancio.create(Long.class)
        );

        organizationsProvider.createOrUpdate(source);

        assertThat(context.fetchCount(Tables.ORGANIZATION)).isEqualTo(1);

        final var actual = context.selectFrom(Tables.ORGANIZATION).fetchSingle();
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(source.getId());
        assertThat(actual.getDigitId().longValue()).isEqualTo(source.getDigitId());
    }

    @Test
    @DisplayName("Проверка получения организации")
    void test_get() {
        final var id = UUID.randomUUID();
        context.insertInto(Tables.ORGANIZATION)
                .set(Tables.ORGANIZATION.ID, id)
                .set(Tables.ORGANIZATION.DIGIT_ID, 1)
                .execute();

        final var actual = organizationsProvider.get(id);
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getDigitId()).isEqualTo(1);
    }
}