package ru.sber.transport.fraud.monitoring.provider.web;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.http.HttpStatus;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.fraud.monitoring.business.FraudDecisionService;
import ru.sber.transport.fraud.monitoring.model.DecisionRequestData;
import ru.sber.transport.fraud.monitoring.web.query.FraudDecisionApiImpl;
import ru.sber.transport.web.api.FraudDecisionApi;
import ru.sber.transport.web.model.DecisionRequest;

import java.util.UUID;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка контроллера для принятия решений по разбирательствам фрода")
class FraudDecisionApiImplTest {

    private final FraudDecisionService fraudDecisionService = mock(FraudDecisionService.class);

    private final FraudDecisionApi controller = new FraudDecisionApiImpl(fraudDecisionService);

    @Test
    @DisplayName("Успешное принятие решения")
    void postFraudDecision_shouldReturnOk() throws Exception {
        final var requestId = UUID.randomUUID();
        final var caseId = UUID.randomUUID();
        final var decision = new DecisionRequest()
                .decision("CONFIRM")
                .reason("потому что");

        doNothing().when(fraudDecisionService).solve(any(), any(), any(DecisionRequestData.class));

        final var resultFuture = controller.postFraudDecision(decision, requestId, caseId);
        final var result = resultFuture.get();

        assertSoftly(it -> {
            it.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            it.assertThat(result.getBody()).isNotNull();
            it.assertThat(result.getBody().getStatus()).isEqualTo("SUCCESS");
            it.assertThat(result.getBody().getRequestId()).isEqualTo(requestId);
        });

        verify(fraudDecisionService).solve(any(), any(), any(DecisionRequestData.class));
    }
}