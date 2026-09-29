package ru.sber.transport.fraud.monitoring.providers.fraud;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.database.fraud_monitoring.tables.records.FraudRecord;
import ru.sber.transport.fraud.monitoring.model.Fraud;

/**
 * Реализация модели нарушения (фрода)
 */
@RequiredArgsConstructor
public class FraudModel implements Fraud {

    @Delegate
    private final FraudRecord source;
}