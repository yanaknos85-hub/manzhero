package ru.sber.transport.fraud.monitoring.messaging.listeners;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.fraud.monitoring.messaging.listeners.mapper.FraudMapper;
import ru.sber.transport.fraud.monitoring.messaging.listeners.message.FraudListenMessage;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.FraudImpl;
import ru.sber.transport.fraud.monitoring.model.Fraud;
import ru.sber.transport.fraud.monitoring.providers.FraudsDatabaseProvider;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка получения данных о фроде из топика service.fraud-monitoring.listen.fraud")
class FraudListenListenerTest {

    private final FraudsDatabaseProvider fraudsDatabaseProvider = mock(FraudsDatabaseProvider.class);
    private final FraudMapper fraudMapper = mock(FraudMapper.class);

    private final Consumer<Message<FraudListenMessage>> input = new FraudListenListener(fraudsDatabaseProvider, fraudMapper);

    @Test
    @DisplayName("Получение данных о фроде из топика для существующей заявки")
    void testSaveFraudForExistingTripRequest() {
        final var requestId = UUID.randomUUID();
        final var source = "request";
        final var comment = "Повторная поездка";
        final var fraudType = "RADIUS";

        final var message = new FraudListenMessage(
                requestId,
                source,
                List.of(new FraudListenMessage.FraudDataItem(comment, null, fraudType))
        );

        when(fraudMapper.toFraudListenItem(any(), any()))
                .thenAnswer(invocation -> {
                    final var payload = invocation.getArgument(0, FraudListenMessage.class);
                    final var item = invocation.getArgument(1, FraudListenMessage.FraudDataItem.class);
                    var fraud = new FraudImpl();
                    fraud.setRequestId(payload.requestId());
                    fraud.setComment(item.comment());
                    fraud.setFraudType(item.type());
                    fraud.setSource(payload.source());
                    return fraud;
                });

        input.accept(MessageBuilder.withPayload(message).build());

        final var fraudCaptor = ArgumentCaptor.forClass(Fraud.class);

        verify(fraudsDatabaseProvider).save(fraudCaptor.capture());

        final var actual = fraudCaptor.getValue();

        assertSoftly(it -> {
            it.assertThat(actual.getRequestId()).isEqualTo(requestId);
            it.assertThat(actual.getComment()).isEqualTo(comment);
            it.assertThat(actual.getFraudType()).isEqualTo(fraudType);
            it.assertThat(actual.getSource()).isEqualTo(source);
        });
    }

    @Test
    @DisplayName("Заполнение source=ai_antifraud для старых заявок с comment '%повторяющиеся заявки%'")
    void testSetAiAntifraudSourceForOldFrauds() {
        final var requestId = UUID.randomUUID();
        final var commentWithPattern = "Подозрение на повторяющиеся заявки";
        final var source = "external_request";

        final var message = new FraudListenMessage(
                requestId,
                source,
                List.of(new FraudListenMessage.FraudDataItem(commentWithPattern, null, "RECEIPT"))
        );

        when(fraudMapper.toFraudListenItem(any(), any()))
                .thenAnswer(invocation -> {
                    final var payload = invocation.getArgument(0, FraudListenMessage.class);
                    final var item = invocation.getArgument(1, FraudListenMessage.FraudDataItem.class);
                    var fraud = new FraudImpl();
                    fraud.setRequestId(payload.requestId());
                    fraud.setComment(item.comment());
                    fraud.setFraudType(item.type());
                    fraud.setSource(payload.source());
                    return fraud;
                });

        input.accept(MessageBuilder.withPayload(message).build());

        final var fraudCaptor = ArgumentCaptor.forClass(Fraud.class);

        verify(fraudsDatabaseProvider).save(fraudCaptor.capture());

        final var actual = fraudCaptor.getValue();

        assertSoftly(it -> {
            it.assertThat(actual.getComment()).contains("повторяющиеся заявки");
            it.assertThat(actual.getSource()).isEqualTo("ai_antifraud");
        });
    }

    @Test
    @DisplayName("Заполнение source=UNKNOWN для старых заявок без паттерна")
    void testSetUnknownSourceForOldFrauds() {
        final var requestId = UUID.randomUUID();
        final var commentWithoutPattern = "Другая причина";
        final var source = "antifrad_radius";

        final var message = new FraudListenMessage(
                requestId,
                source,
                List.of(new FraudListenMessage.FraudDataItem(commentWithoutPattern, null, "SPLIT"))
        );

        when(fraudMapper.toFraudListenItem(any(), any()))
                .thenAnswer(invocation -> {
                    final var payload = invocation.getArgument(0, FraudListenMessage.class);
                    final var item = invocation.getArgument(1, FraudListenMessage.FraudDataItem.class);
                    var fraud = new FraudImpl();
                    fraud.setRequestId(payload.requestId());
                    fraud.setComment(item.comment());
                    fraud.setFraudType(item.type());
                    fraud.setSource(payload.source());
                    return fraud;
                });

        input.accept(MessageBuilder.withPayload(message).build());

        final var fraudCaptor = ArgumentCaptor.forClass(Fraud.class);

        verify(fraudsDatabaseProvider).save(fraudCaptor.capture());

        final var actual = fraudCaptor.getValue();

        assertSoftly(it -> {
            it.assertThat(actual.getComment()).doesNotContain("повторяющиеся заявки");
            it.assertThat(actual.getSource()).isEqualTo(source);
        });
    }

    @Test
    @DisplayName("Обработка нескольких элементов fraudData")
    void testSaveMultipleFraudDataItems() {
        when(fraudMapper.toFraudListenItem(any(), any()))
                .thenAnswer(invocation -> {
                    final var payload = invocation.getArgument(0, FraudListenMessage.class);
                    final var item = invocation.getArgument(1, FraudListenMessage.FraudDataItem.class);
                    var fraud = new FraudImpl();
                    fraud.setRequestId(payload.requestId());
                    fraud.setComment(item.comment());
                    fraud.setFraudType(item.type());
                    fraud.setSource(payload.source());
                    return fraud;
                });

        final var requestId = UUID.randomUUID();
        final var source = "request";

        final var message = new FraudListenMessage(
                requestId,
                source,
                List.of(new FraudListenMessage.FraudDataItem("First fraud", null, "RADIUS"), new FraudListenMessage.FraudDataItem("Second fraud", null, "SPLIT"))
        );

        input.accept(MessageBuilder.withPayload(message).build());

        verify(fraudsDatabaseProvider, times(2)).save(any(Fraud.class));
    }

}
