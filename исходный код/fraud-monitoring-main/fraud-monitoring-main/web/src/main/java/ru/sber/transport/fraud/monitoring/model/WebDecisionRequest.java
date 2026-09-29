package ru.sber.transport.fraud.monitoring.model;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.fraud.monitoring.model.DecisionRequestData;
import ru.sber.transport.web.model.DecisionRequest;

/**
 * Объект запроса на решение по разбирательству фрода
 */
@RequiredArgsConstructor
public class WebDecisionRequest implements DecisionRequestData {

    @Delegate
    private final DecisionRequest delegate;
}