package ru.sber.transport.fraud.monitoring.business.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.fraud.monitoring.business.FraudDecisionService;
import ru.sber.transport.fraud.monitoring.model.DecisionRequestData;
import ru.sber.transport.fraud.monitoring.model.FraudCaseDataMarker;
import ru.sber.transport.fraud.monitoring.providers.FraudCasesDataBaseProvider;

import java.util.UUID;


/**
 * Реализация сервиса {@link FraudDecisionService}.
 * <p>
 * Получает кейс фрода по идентификатору, обновляет вердикт, причину
 * и снимает флаг необходимости валидации.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class FraudDecisionServiceImpl implements FraudDecisionService {

    private final FraudCasesDataBaseProvider fraudCasesDataBaseProvider;

    @Override
    public void solve(UUID requestId, UUID caseId, DecisionRequestData decisionData) {
        log.debug("Получение кейса фрода для решения: requestId={}, caseId={}", requestId, caseId);

        var fraudCase = fraudCasesDataBaseProvider.getFraudCaseByFraudCaseId(caseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Кейс фрода не найден: caseId=" + caseId));

        log.debug("Сохранение решения по кейсу фрода: caseId={}, decision={}", caseId, decisionData.getDecision());
        fraudCasesDataBaseProvider.solveFraudCase(fraudCase, decisionData.getDecision(), decisionData.getReason());
        log.info("Решение по кейсу фрода сохранено: caseId={}, decision={}", caseId, decisionData.getDecision());
    }
}