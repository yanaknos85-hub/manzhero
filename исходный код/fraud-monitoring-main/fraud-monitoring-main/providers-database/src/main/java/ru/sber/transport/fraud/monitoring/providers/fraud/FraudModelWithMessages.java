package ru.sber.transport.fraud.monitoring.providers.fraud;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.database.fraud_monitoring.tables.records.FraudRecord;
import ru.sber.transport.fraud.monitoring.model.FraudCaseData;

import java.util.Collections;
import java.util.List;

/**
 * Реализация модели нарушения (фрода)
 */
@RequiredArgsConstructor
public class FraudModelWithMessages implements FraudCaseData {

    @Delegate
    private final FraudRecord source;

    @Override
    public boolean isNeedValidation() {
        return source.getNeedValidation();
    }

    @Override
    public List<? extends FraudMessageItem> getMessaging() {
        return Collections.emptyList();
    }
}