package ru.sber.transport.fraud.monitoring.providers.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sber.transport.fraud.monitoring.model.FraudCaseDataMarker;

import java.util.UUID;

/**
 * Тестовая реализация {@link FraudCaseDataMarker} для embedded postgres тестов
 */
@Getter
@RequiredArgsConstructor
public class TestFraudCaseDataMaker implements FraudCaseDataMarker {

    private final UUID id;
    private final UUID requestId;
    private final String comment;
    private final String fraudType;
    private final String source;
    private final String aiVerdict;
    private final boolean needValidation;
}