package ru.sber.transport.fraud.monitoring.business;

import ru.sber.transport.fraud.monitoring.model.DecisionRequestData;

import java.util.UUID;

/**
 * Сервис для обработки решений по разбирательствам фрода.
 * <p>
 * Принимает решение сотрудника УТО и сохраняет его в БД.</p>
 */
public interface FraudDecisionService {

    /**
     * Сохраняет решение по разбирательству фрода.
     * <p>
     * Обновляет запись в таблице {@code FRAUD}: устанавливает {@code VERDICT},
     * {@code REASON} и {@code NEED_VALIDATION = false}.</p>
     *
     * @param requestId     идентификатор заявки
     * @param caseId        идентификатор разбирательства
     * @param decisionData  решение сотрудника
     */
    void solve(UUID requestId, UUID caseId, DecisionRequestData decisionData);
}