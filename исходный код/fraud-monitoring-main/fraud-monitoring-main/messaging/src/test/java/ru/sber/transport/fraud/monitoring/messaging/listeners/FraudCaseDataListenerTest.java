package ru.sber.transport.fraud.monitoring.messaging.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.qameta.allure.Feature;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.fraud.monitoring.business.FraudCaseDataService;
import ru.sber.transport.fraud.monitoring.messaging.listeners.mapper.FraudCaseDataMapper;
import ru.sber.transport.fraud.monitoring.messaging.listeners.message.FraudCaseDataMessage;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.FraudCaseDataImpl;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.FraudMessageItemImpl;
import ru.sber.transport.fraud.monitoring.model.FraudCaseData;
import ru.sber.transport.fraud.monitoring.model.FraudConstants;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка получения данных кейса фрода из топика service.fraud-monitoring.listen.cases_data")
class FraudCaseDataListenerTest {

    private final FraudCaseDataService fraudCaseDataService = mock(FraudCaseDataService.class);
    private final FraudCaseDataMapper fraudCaseDataMapper = mock(FraudCaseDataMapper.class);

    private final Consumer<Message<FraudCaseDataMessage>> input = new FraudCaseDataListener(
            fraudCaseDataService, fraudCaseDataMapper
    );

    @Test
    @DisplayName("Успешная обработка сообщения с email-сообщениями: маппинг и сохранение")
    void test_accept_withMessaging() {
        final var fraudCaseId = UUID.randomUUID();
        final var messageItem = new FraudCaseDataMessage.MessageItem(
                LocalDateTime.now(),
                "from@test.com",
                "to@test.com",
                "Тело письма"
        );
        final var message = new FraudCaseDataMessage(
                fraudCaseId,
                List.of(messageItem),
                Instancio.create(String.class),
                Instancio.create(String.class),
                true
        );

        var mappedFraudCaseData = new FraudCaseDataImpl();
        mappedFraudCaseData.setId(fraudCaseId);
        mappedFraudCaseData.setAiVerdict(message.aiVerdict());
        mappedFraudCaseData.setNeedValidation(message.needValidation());

        var mappedMessageItem = FraudMessageItemImpl.builder()
                .messageDate(messageItem.messageDate())
                .fromEmail(messageItem.fromEmail())
                .toEmail(messageItem.toEmail())
                .body(messageItem.body())
                .build();

        mappedFraudCaseData.setMessaging(List.of(mappedMessageItem));

        when(fraudCaseDataMapper.toFraudCaseData(any())).thenReturn(mappedFraudCaseData);

        input.accept(MessageBuilder.withPayload(message).build());

        verify(fraudCaseDataMapper).toFraudCaseData(message);

        var captor = ArgumentCaptor.forClass(FraudCaseData.class);
        verify(fraudCaseDataService).updateFraudCaseData(captor.capture());

        var actual = captor.getValue();
        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(fraudCaseId);
            it.assertThat(actual.getAiVerdict()).isEqualTo(message.aiVerdict());
            it.assertThat(actual.isNeedValidation()).isTrue();
            it.assertThat(actual.getMessaging()).isNotNull();
            it.assertThat(actual.getMessaging()).hasSize(1);
            it.assertThat(actual.getMessaging().get(0).getFromEmail()).isEqualTo("from@test.com");
        });
    }

    @Test
    @DisplayName("Сообщение без id: логируется предупреждение, сервис не вызывается")
    void test_accept_nullId() {
        var message = new FraudCaseDataMessage(
                null,
                List.of(),
                Instancio.create(String.class),
                Instancio.create(String.class),
                false
        );

        input.accept(MessageBuilder.withPayload(message).build());

        verifyNoInteractions(fraudCaseDataMapper);
        verifyNoInteractions(fraudCaseDataService);
    }

    @Test
    @DisplayName("Сообщение без email-сообщений: кейс сохраняется, сообщения не сохраняются")
    void test_accept_emptyMessaging() {
        final var fraudCaseId = UUID.randomUUID();
        var message = new FraudCaseDataMessage(
                fraudCaseId,
                List.of(),
                Instancio.create(String.class),
                Instancio.create(String.class),
                false
        );

        var mappedFraudCaseData = new FraudCaseDataImpl();
        mappedFraudCaseData.setId(fraudCaseId);
        mappedFraudCaseData.setMessaging(List.of());

        when(fraudCaseDataMapper.toFraudCaseData(any())).thenReturn(mappedFraudCaseData);

        input.accept(MessageBuilder.withPayload(message).build());

        var captor = ArgumentCaptor.forClass(FraudCaseData.class);
        verify(fraudCaseDataService).updateFraudCaseData(captor.capture());

        var actual = captor.getValue();
        assertThat(actual.getMessaging()).isEmpty();
    }

    @Test
    @DisplayName("aiVerdict = NOT_FRAUD: cleared вычисляется как true")
    void test_accept_aiVerdictNotFraud_setsClearedTrue() {
        final var fraudCaseId = UUID.randomUUID();
        var message = new FraudCaseDataMessage(
                fraudCaseId,
                List.of(),
                FraudConstants.NOT_FRAUD_VERDICT,
                "Some comment",
                false
        );

        var mappedFraudCaseData = new FraudCaseDataImpl();
        mappedFraudCaseData.setId(fraudCaseId);
        mappedFraudCaseData.setAiVerdict(FraudConstants.NOT_FRAUD_VERDICT);
        mappedFraudCaseData.setMessaging(List.of());

        when(fraudCaseDataMapper.toFraudCaseData(any())).thenReturn(mappedFraudCaseData);

        input.accept(MessageBuilder.withPayload(message).build());

        var captor = ArgumentCaptor.forClass(FraudCaseData.class);
        verify(fraudCaseDataService).updateFraudCaseData(captor.capture());

        var actual = captor.getValue();
        assertSoftly(it -> it.assertThat(actual.getAiVerdict()).isEqualTo(FraudConstants.NOT_FRAUD_VERDICT));
    }

    @Test
    @DisplayName("aiVerdict != NOT_FRAUD: cleared = false")
    void test_accept_aiVerdictOther_setsClearedFalse() {
        final var fraudCaseId = UUID.randomUUID();
        var message = new FraudCaseDataMessage(
                fraudCaseId,
                List.of(),
                "FRAUD_CONFIRMED",
                "Some comment",
                true
        );

        var mappedFraudCaseData = new FraudCaseDataImpl();
        mappedFraudCaseData.setId(fraudCaseId);
        mappedFraudCaseData.setAiVerdict("FRAUD_CONFIRMED");
        mappedFraudCaseData.setMessaging(List.of());

        when(fraudCaseDataMapper.toFraudCaseData(any())).thenReturn(mappedFraudCaseData);

        input.accept(MessageBuilder.withPayload(message).build());

        var captor = ArgumentCaptor.forClass(FraudCaseData.class);
        verify(fraudCaseDataService).updateFraudCaseData(captor.capture());

        var actual = captor.getValue();
        assertThat(actual.getAiVerdict()).isNotEqualTo(FraudConstants.NOT_FRAUD_VERDICT);
    }
}