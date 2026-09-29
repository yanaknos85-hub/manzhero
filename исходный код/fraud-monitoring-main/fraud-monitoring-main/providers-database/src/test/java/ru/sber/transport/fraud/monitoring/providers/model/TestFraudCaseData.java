package ru.sber.transport.fraud.monitoring.providers.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sber.transport.fraud.monitoring.model.FraudCaseData;

import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class TestFraudCaseData implements FraudCaseData {
    private final UUID id;
    private final String aiVerdict;
    private final String aiComment;
    private final String comment;
    private final boolean needValidation;
    private final List<FraudMessageItem> messaging;
}
