package ru.sber.transport.fraud.monitoring.providers.fraud_case.model;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.database.fraud_monitoring.tables.records.FraudRecord;
import ru.sber.transport.fraud.monitoring.model.FraudCaseDataMarker;

import java.util.UUID;

/**
 * Реализация {@link FraudCaseDataMarker} через делегат к {@link FraudRecord}
 */
@Builder
@RequiredArgsConstructor
public class FraudCaseDataMarkerModel implements FraudCaseDataMarker {

    @Delegate(types = MarkerDelegates.class)
    private final FraudRecord source;

    @Override
    public String getAiVerdict() {
        return source.getAiVerdict();
    }

    @Override
    public boolean isNeedValidation() {
        return source.getNeedValidation() != null && source.getNeedValidation();
    }

    private interface MarkerDelegates {
        UUID getId();
        UUID getRequestId();
        String getComment();
        String getFraudType();
        String getSource();
    }
}