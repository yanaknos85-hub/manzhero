package ru.sber.transport.fraud.monitoring.business.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.sber.transport.fraud.monitoring.model.FraudCaseData;

import java.util.List;
import java.util.UUID;

/**
 * Тестовая реализация {@link FraudCaseData} для модульных тестов
 */
@Getter
@AllArgsConstructor
public class TestFraudCaseData implements FraudCaseData {

    private final UUID id;
    private final String aiVerdict;
    private final String aiComment;
    private final String comment;
    private final boolean needValidation;
    private final List<? extends FraudCaseData.FraudMessageItem> messaging;
}