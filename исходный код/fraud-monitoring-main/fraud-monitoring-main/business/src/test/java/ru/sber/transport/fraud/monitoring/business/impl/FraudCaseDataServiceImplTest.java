package ru.sber.transport.fraud.monitoring.business.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.fraud.monitoring.business.model.TestFraudCaseData;
import ru.sber.transport.fraud.monitoring.business.model.TestFraudMessageItem;
import ru.sber.transport.fraud.monitoring.model.FraudCaseDataMarker;
import ru.sber.transport.fraud.monitoring.providers.FraudCasesDataBaseProvider;
import ru.sber.transport.fraud.monitoring.providers.FraudMessageItemDataBaseProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка сервиса сохранения кейсов фрода")
class FraudCaseDataServiceImplTest {

    private final FraudCasesDataBaseProvider fraudCasesDataBaseProvider = mock(FraudCasesDataBaseProvider.class);

    private final FraudMessageItemDataBaseProvider fraudMessageItemDataBaseProvider =
            mock(FraudMessageItemDataBaseProvider.class);

    private final FraudCaseDataServiceImpl fraudCaseDataService =
            new FraudCaseDataServiceImpl(fraudCasesDataBaseProvider, fraudMessageItemDataBaseProvider);

    @Test
    @DisplayName("Кейс найден — обновление кейса и сохранение email-сообщений")
    void test_updateFraudCaseData_shouldUpdateAndSaveMessages() {
        var fraudId = UUID.randomUUID();
        var existingMarker = mock(FraudCaseDataMarker.class);
        var messageItem = new TestFraudMessageItem(LocalDateTime.now(), "from@test.ru", "to@test.ru", "body");
        var fraudCaseData = new TestFraudCaseData(fraudId, "verdict", "aiComment", "comment", true, List.of(messageItem));

        when(fraudCasesDataBaseProvider.getFraudCaseByFraudCaseId(fraudId))
                .thenReturn(Optional.of(existingMarker));

        fraudCaseDataService.updateFraudCaseData(fraudCaseData);

        verify(fraudCasesDataBaseProvider).getFraudCaseByFraudCaseId(fraudId);
        verify(fraudCasesDataBaseProvider).updateFraudCase(existingMarker, fraudCaseData);
        verify(fraudMessageItemDataBaseProvider).saveFraudMessageItem(List.of(messageItem), fraudId);
    }

    @Test
    @DisplayName("Кейс не найден — пропуск обновления и сохранения")
    void test_updateFraudCaseData_shouldSkipWhenCaseNotFound() {
        var fraudId = UUID.randomUUID();
        var fraudCaseData = new TestFraudCaseData(fraudId, "verdict", "aiComment", "comment", false, List.of());

        when(fraudCasesDataBaseProvider.getFraudCaseByFraudCaseId(fraudId))
                .thenReturn(Optional.empty());

        fraudCaseDataService.updateFraudCaseData(fraudCaseData);

        verify(fraudCasesDataBaseProvider).getFraudCaseByFraudCaseId(fraudId);
        verify(fraudCasesDataBaseProvider, never()).updateFraudCase(any(), any());
        verify(fraudMessageItemDataBaseProvider, never()).saveFraudMessageItem(anyList(), any());
    }

    @Test
    @DisplayName("Список сообщений пуст — сохранение сообщений пропущено")
    void test_updateFraudCaseData_shouldSkipWhenMessagingEmpty() {
        var fraudId = UUID.randomUUID();
        var existingMarker = mock(FraudCaseDataMarker.class);
        var fraudCaseData = new TestFraudCaseData(fraudId, "verdict", "aiComment", "comment", false, List.of());

        when(fraudCasesDataBaseProvider.getFraudCaseByFraudCaseId(fraudId))
                .thenReturn(Optional.of(existingMarker));

        fraudCaseDataService.updateFraudCaseData(fraudCaseData);

        verify(fraudCasesDataBaseProvider).getFraudCaseByFraudCaseId(fraudId);
        verify(fraudCasesDataBaseProvider).updateFraudCase(existingMarker, fraudCaseData);
        verify(fraudMessageItemDataBaseProvider, never()).saveFraudMessageItem(anyList(), any());
    }

    @Test
    @DisplayName("Список сообщений null — сохранение сообщений пропущено")
    void test_updateFraudCaseData_shouldSkipWhenMessagingNull() {
        var fraudId = UUID.randomUUID();
        var existingMarker = mock(FraudCaseDataMarker.class);
        var fraudCaseData = new TestFraudCaseData(fraudId, "verdict", "aiComment", "comment", true, null);

        when(fraudCasesDataBaseProvider.getFraudCaseByFraudCaseId(fraudId))
                .thenReturn(Optional.of(existingMarker));

        fraudCaseDataService.updateFraudCaseData(fraudCaseData);

        verify(fraudCasesDataBaseProvider).getFraudCaseByFraudCaseId(fraudId);
        verify(fraudCasesDataBaseProvider).updateFraudCase(existingMarker, fraudCaseData);
        verify(fraudMessageItemDataBaseProvider, never()).saveFraudMessageItem(anyList(), any());
    }
}