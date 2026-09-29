package ru.sber.transport.fraud.monitoring.providers.fraud;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.database.fraud_monitoring.tables.records.FraudRecord;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка модели нарушения с сообщениями")
class FraudModelWithMessagesTest {

    @Test
    @DisplayName("getMessaging всегда возвращает пустой список")
    void test_getMessaging_returnsEmptyList() {
        final var fraudRecord = new FraudRecord();
        fraudRecord.setId(UUID.randomUUID());

        final var model = new FraudModelWithMessages(fraudRecord);

        assertThat(model.getMessaging()).isEmpty();
    }

    @Test
    @DisplayName("isNeedValidation возвращает значение из записи")
    void test_isNeedValidation_returnsFromRecord() {
        final var fraudRecord = new FraudRecord();
        fraudRecord.setId(UUID.randomUUID());
        fraudRecord.setNeedValidation(true);

        final var model = new FraudModelWithMessages(fraudRecord);

        assertThat(model.isNeedValidation()).isTrue();
    }

    @Test
    @DisplayName("isNeedValidation возвращает false, если в записи false")
    void test_isNeedValidation_returnsFalse() {
        final var fraudRecord = new FraudRecord();
        fraudRecord.setId(UUID.randomUUID());
        fraudRecord.setNeedValidation(false);

        final var model = new FraudModelWithMessages(fraudRecord);

        assertThat(model.isNeedValidation()).isFalse();
    }

    @Test
    @DisplayName("getAiVerdict делегируется в запись")
    void test_getAiVerdict_delegatesToRecord() {
        final var fraudRecord = new FraudRecord();
        fraudRecord.setId(UUID.randomUUID());
        fraudRecord.setAiVerdict("FRAUD");

        final var model = new FraudModelWithMessages(fraudRecord);

        assertThat(model.getAiVerdict()).isEqualTo("FRAUD");
    }

    @Test
    @DisplayName("getFraudType делегируется в запись")
    void test_getFraudType_delegatesToRecord() {
        final var fraudRecord = new FraudRecord();
        fraudRecord.setId(UUID.randomUUID());
        fraudRecord.setFraudType("ROUTE_FRAUD");

        final var model = new FraudModelWithMessages(fraudRecord);

        assertThat(model.getFraudType()).isEqualTo("ROUTE_FRAUD");
    }

    @Test
    @DisplayName("getSource делегируется в запись")
    void test_getSource_delegatesToRecord() {
        final var fraudRecord = new FraudRecord();
        fraudRecord.setId(UUID.randomUUID());
        fraudRecord.setSource("AUTO_CHECK");

        final var model = new FraudModelWithMessages(fraudRecord);

        assertThat(model.getSource()).isEqualTo("AUTO_CHECK");
    }

    @Test
    @DisplayName("getComment делегируется в запись")
    void test_getComment_delegatesToRecord() {
        final var fraudRecord = new FraudRecord();
        fraudRecord.setId(UUID.randomUUID());
        fraudRecord.setComment("Подозрительный маршрут");

        final var model = new FraudModelWithMessages(fraudRecord);

        assertThat(model.getComment()).isEqualTo("Подозрительный маршрут");
    }

    @Test
    @DisplayName("getId делегируется в запись")
    void test_getId_delegatesToRecord() {
        final var id = UUID.randomUUID();
        final var fraudRecord = new FraudRecord();
        fraudRecord.setId(id);

        final var model = new FraudModelWithMessages(fraudRecord);

        assertThat(model.getId()).isEqualTo(id);
    }
}