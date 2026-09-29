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
import ru.sber.transport.fraud.monitoring.providers.fraud_case.FraudCasesDataBaseProviderImpl;
import ru.sber.transport.fraud.monitoring.providers.model.TestFraudCaseData;
import ru.sber.transport.fraud.monitoring.providers.model.TestFraudCaseDataMaker;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка провайдера данных кейса фрода")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class FraudCasesDataBaseProviderImplTest {

    @Autowired
    private DSLContext context;

    private final FraudCasesDataBaseProvider fraudCasesDataBaseProvider = new FraudCasesDataBaseProviderImpl() {

        @Override
        public DSLContext context() {
            return context;
        }
    };

    @Test
    @DisplayName("getFraudCaseByFraudCaseId: запись найдена, возвращается Optional с маркером")
    void test_getFraudCaseByFraudCaseId_found() {
        var fraudId = UUID.randomUUID();
        var requestId = UUID.randomUUID();

        var fraudRecord = context.newRecord(Tables.FRAUD);
        fraudRecord.setId(fraudId);
        fraudRecord.setRequestId(requestId);
        fraudRecord.setComment("test comment");
        fraudRecord.setFraudType("RADIUS");
        fraudRecord.setSource("ai_antifraud");
        fraudRecord.setAiVerdict("FRAUD_CONFIRMED");
        fraudRecord.setNeedValidation(true);
        fraudRecord.store();

        var result = fraudCasesDataBaseProvider.getFraudCaseByFraudCaseId(fraudId);

        assertSoftly(it -> {
            it.assertThat(result).isPresent();
            var marker = result.get();
            it.assertThat(marker.getId()).isEqualTo(fraudId);
            it.assertThat(marker.getRequestId()).isEqualTo(requestId);
            it.assertThat(marker.getComment()).isEqualTo("test comment");
            it.assertThat(marker.getFraudType()).isEqualTo("RADIUS");
            it.assertThat(marker.getSource()).isEqualTo("ai_antifraud");
            it.assertThat(marker.getAiVerdict()).isEqualTo("FRAUD_CONFIRMED");
            it.assertThat(marker.isNeedValidation()).isTrue();
        });
    }

    @Test
    @DisplayName("getFraudCaseByFraudCaseId: запись не найдена, возвращается пустой Optional")
    void test_getFraudCaseByFraudCaseId_notFound() {
        var result = fraudCasesDataBaseProvider.getFraudCaseByFraudCaseId(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("updateFraudCase: обновление существующего кейса фрода в таблице FRAUD")
    void test_updateFraudCase_update() {
        var fraudId = UUID.randomUUID();
        var requestId = UUID.randomUUID();

        var fraudRecord = context.newRecord(Tables.FRAUD);
        fraudRecord.setId(fraudId);
        fraudRecord.setRequestId(requestId);
        fraudRecord.setComment("old comment");
        fraudRecord.setFraudType("RADIUS");
        fraudRecord.setSource("old source");
        fraudRecord.setAiVerdict("old verdict");
        fraudRecord.setNeedValidation(false);
        fraudRecord.store();

        var marker = new TestFraudCaseDataMaker(
                fraudId, requestId, "new comment", "SPLIT", "new source",
                "old verdict", false
        );

        TestFraudCaseData data = new TestFraudCaseData(fraudId, "new verdict", "aiComment", "comment", true, List.of());

        fraudCasesDataBaseProvider.updateFraudCase(marker, data);

        var single = context.selectFrom(Tables.FRAUD)
                .where(Tables.FRAUD.ID.eq(fraudId))
                .fetchSingle();

        assertSoftly(it -> {
            it.assertThat(single).isNotNull();
            it.assertThat(single.getId()).isEqualTo(fraudId);
            it.assertThat(single.getComment()).isEqualTo("new comment");
            it.assertThat(single.getFraudType()).isEqualTo("SPLIT");
            it.assertThat(single.getSource()).isEqualTo("new source");
            it.assertThat(single.getAiVerdict()).isEqualTo("new verdict");
            it.assertThat(single.getAiComment()).isEqualTo("aiComment");
            it.assertThat(single.getNeedValidation()).isTrue();
            it.assertThat(single.getCleared()).isFalse();
        });
    }

    @Test
    @DisplayName("updateFraudCase: aiVerdict=NOT_FRAUD проставляет cleared=true")
    void test_updateFraudCase_notFraudVerdictSetsClearedTrue() {
        var fraudId = UUID.randomUUID();
        var requestId = UUID.randomUUID();

        var fraudRecord = context.newRecord(Tables.FRAUD);
        fraudRecord.setId(fraudId);
        fraudRecord.setRequestId(requestId);
        fraudRecord.setComment("comment");
        fraudRecord.setFraudType("RADIUS");
        fraudRecord.setSource("source");
        fraudRecord.setAiVerdict("FRAUD_CONFIRMED");
        fraudRecord.setNeedValidation(true);
        fraudRecord.store();

        var marker = new TestFraudCaseDataMaker(
                fraudId, requestId, "comment", "RADIUS", "source",
                "FRAUD_CONFIRMED", true
        );

        TestFraudCaseData data = new TestFraudCaseData(
                fraudId, "NOT_FRAUD", "aiComment", "comment", true, List.of()
        );

        fraudCasesDataBaseProvider.updateFraudCase(marker, data);

        var single = context.selectFrom(Tables.FRAUD)
                .where(Tables.FRAUD.ID.eq(fraudId))
                .fetchSingle();

        assertSoftly(it -> {
            it.assertThat(single.getAiVerdict()).isEqualTo("NOT_FRAUD");
            it.assertThat(single.getCleared()).isTrue();
        });
    }

    @Test
    @DisplayName("updateFraudCase: null — ничего не сохраняется")
    void test_updateFraudCase_null() {
        fraudCasesDataBaseProvider.updateFraudCase(null, null);

        assertThat(context.fetchCount(Tables.FRAUD)).isZero();
    }

    @Test
    @DisplayName("solveFraudCase: обновление VERDICT, REASON и NEED_VALIDATION")
    void test_solveFraudCase_shouldUpdateVerdictReasonAndNeedValidation() {
        var fraudId = UUID.randomUUID();
        var requestId = UUID.randomUUID();

        var fraudRecord = context.newRecord(Tables.FRAUD);
        fraudRecord.setId(fraudId);
        fraudRecord.setRequestId(requestId);
        fraudRecord.setComment("test comment");
        fraudRecord.setFraudType("RADIUS");
        fraudRecord.setSource("ai_antifraud");
        fraudRecord.setAiVerdict("SUSPICIOUS");
        fraudRecord.setNeedValidation(true);
        fraudRecord.store();

        var marker = fraudCasesDataBaseProvider.getFraudCaseByFraudCaseId(fraudId).orElseThrow();

        fraudCasesDataBaseProvider.solveFraudCase(marker, "CONFIRM", "потому что");

        var single = context.selectFrom(Tables.FRAUD)
                .where(Tables.FRAUD.ID.eq(fraudId))
                .fetchSingle();

        assertSoftly(it -> {
            it.assertThat(single.getVerdict()).isEqualTo("CONFIRM");
            it.assertThat(single.getReason()).isEqualTo("потому что");
            it.assertThat(single.getNeedValidation()).isFalse();
            it.assertThat(single.getCleared()).isFalse();
        });
    }

    @Test
    @DisplayName("solveFraudCase: NOT_FRAUD проставляет cleared=true")
    void test_solveFraudCase_notFraud_shouldSetClearedTrue() {
        var fraudId = UUID.randomUUID();
        var requestId = UUID.randomUUID();

        var fraudRecord = context.newRecord(Tables.FRAUD);
        fraudRecord.setId(fraudId);
        fraudRecord.setRequestId(requestId);
        fraudRecord.setComment("test comment");
        fraudRecord.setFraudType("RADIUS");
        fraudRecord.setSource("ai_antifraud");
        fraudRecord.setAiVerdict("SUSPICIOUS");
        fraudRecord.setNeedValidation(true);
        fraudRecord.store();

        var marker = fraudCasesDataBaseProvider.getFraudCaseByFraudCaseId(fraudId).orElseThrow();

        fraudCasesDataBaseProvider.solveFraudCase(marker, "NOT_FRAUD", "ошибочное срабатывание");

        var single = context.selectFrom(Tables.FRAUD)
                .where(Tables.FRAUD.ID.eq(fraudId))
                .fetchSingle();

        assertSoftly(it -> {
            it.assertThat(single.getVerdict()).isEqualTo("NOT_FRAUD");
            it.assertThat(single.getReason()).isEqualTo("ошибочное срабатывание");
            it.assertThat(single.getNeedValidation()).isFalse();
            it.assertThat(single.getCleared()).isTrue();
        });
    }

    @Test
    @DisplayName("solveFraudCase: null — ничего не обновляется")
    void test_solveFraudCase_null() {
        fraudCasesDataBaseProvider.solveFraudCase(null, "CONFIRM", "потому что");

        assertThat(context.fetchCount(Tables.FRAUD)).isZero();
    }
}