package ru.sber.transport.fraud.monitoring.providers;

import io.qameta.allure.Feature;
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
import ru.sber.transport.fraud.monitoring.providers.fraud_case.FraudMessageItemDataBaseProviderImpl;
import ru.sber.transport.fraud.monitoring.providers.model.TestFraudMessageItem;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sber.transport.fraud.monitoring.model.FraudCaseData.FraudMessageItem;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка провайдера сохранения email-сообщений кейса фрода")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class FraudMessageItemDataBaseProviderImplTest {

    @Autowired
    private DSLContext context;

    private final FraudMessageItemDataBaseProviderImpl provider = new FraudMessageItemDataBaseProviderImpl() {
        @Override
        public DSLContext context() {
            return context;
        }
    };

    @Test
    @DisplayName("saveFraudMessageItem: сохранение одного email-сообщения")
    void test_saveFraudMessageItem_single() {
        var fraudId = UUID.randomUUID();
        var messageDate = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        var items = List.<FraudMessageItem>of(
                new TestFraudMessageItem(messageDate, "from@test.ru", "to@test.ru", "body text"));

        provider.saveFraudMessageItem(items, fraudId);

        var records = context.selectFrom(Tables.MESSAGING)
                .where(Tables.MESSAGING.FRAUD_CASE_ID.eq(fraudId))
                .fetch();

        assertThat(records).hasSize(1);
        var messagingRecord = records.getFirst();
        assertThat(messagingRecord.getFraudCaseId()).isEqualTo(fraudId);
        assertThat(messagingRecord.getMessageDate()).isEqualTo(messageDate);
        assertThat(messagingRecord.getFromEmail()).isEqualTo("from@test.ru");
        assertThat(messagingRecord.getToEmail()).isEqualTo("to@test.ru");
        assertThat(messagingRecord.getBody()).isEqualTo("body text");
    }

    @Test
    @DisplayName("saveFraudMessageItem: сохранение нескольких email-сообщений")
    void test_saveFraudMessageItem_multiple() {
        var fraudId = UUID.randomUUID();
        var items = List.<FraudMessageItem>of(
                new TestFraudMessageItem(LocalDateTime.now(), "a@test.ru", "b@test.ru", "body1"),
                new TestFraudMessageItem(LocalDateTime.now(), "c@test.ru", "d@test.ru", "body2"));

        provider.saveFraudMessageItem(items, fraudId);

        var records = context.selectFrom(Tables.MESSAGING)
                .where(Tables.MESSAGING.FRAUD_CASE_ID.eq(fraudId))
                .fetch();

        assertThat(records).hasSize(2);
    }

    @Test
    @DisplayName("saveFraudMessageItem: null — ничего не сохраняется")
    void test_saveFraudMessageItem_null() {
        provider.saveFraudMessageItem(null, UUID.randomUUID());

        assertThat(context.fetchCount(Tables.MESSAGING)).isZero();
    }

    @Test
    @DisplayName("saveFraudMessageItem: пустой список — ничего не сохраняется")
    void test_saveFraudMessageItem_empty() {
        provider.saveFraudMessageItem(List.of(), UUID.randomUUID());

        assertThat(context.fetchCount(Tables.MESSAGING)).isZero();
    }
}