package ru.sber.transport.fraud.monitoring.business.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.fraud.monitoring.model.FraudCaseDataMarker;
import ru.sber.transport.fraud.monitoring.providers.FraudCasesDataBaseProvider;

import ru.sber.transport.fraud.monitoring.model.DecisionRequestData;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка сервиса принятия решений по разбирательствам фрода")
class FraudDecisionServiceImplTest {

    private final FraudCasesDataBaseProvider fraudCasesDataBaseProvider = mock(FraudCasesDataBaseProvider.class);

    private final FraudDecisionServiceImpl service = new FraudDecisionServiceImpl(fraudCasesDataBaseProvider);

    @Test
    @DisplayName("Успешное сохранение решения")
    void solve_shouldSaveDecision() {
        final var requestId = UUID.randomUUID();
        final var caseId = UUID.randomUUID();
        final var decisionData = mock(DecisionRequestData.class);
        when(decisionData.getDecision()).thenReturn("CONFIRM");
        when(decisionData.getReason()).thenReturn("потому что");

        final var fraudCase = mock(FraudCaseDataMarker.class);
        when(fraudCase.getId()).thenReturn(caseId);
        when(fraudCasesDataBaseProvider.getFraudCaseByFraudCaseId(caseId))
                .thenReturn(Optional.of(fraudCase));

        service.solve(requestId, caseId, decisionData);

        verify(fraudCasesDataBaseProvider).getFraudCaseByFraudCaseId(caseId);
        verify(fraudCasesDataBaseProvider).solveFraudCase(fraudCase, "CONFIRM", "потому что");
    }

    @Test
    @DisplayName("Ошибка при несуществующем кейсе фрода")
    void solve_shouldThrowExceptionWhenCaseNotFound() {
        final var requestId = UUID.randomUUID();
        final var caseId = UUID.randomUUID();
        final var decisionData = mock(DecisionRequestData.class);
        when(decisionData.getDecision()).thenReturn("CONFIRM");
        when(decisionData.getReason()).thenReturn("потому что");

        when(fraudCasesDataBaseProvider.getFraudCaseByFraudCaseId(caseId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.solve(requestId, caseId, decisionData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(caseId.toString());

        verify(fraudCasesDataBaseProvider).getFraudCaseByFraudCaseId(caseId);
        verifyNoMoreInteractions(fraudCasesDataBaseProvider);
    }
}