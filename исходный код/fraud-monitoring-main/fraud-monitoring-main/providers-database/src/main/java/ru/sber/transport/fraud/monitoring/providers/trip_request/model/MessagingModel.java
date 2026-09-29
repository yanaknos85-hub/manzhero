package ru.sber.transport.fraud.monitoring.providers.trip_request.model;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.database.fraud_monitoring.tables.records.MessagingRecord;

import static ru.sber.transport.fraud.monitoring.model.FraudCaseData.*;

@RequiredArgsConstructor
public class MessagingModel implements FraudMessageItem {
    @Delegate
    private final MessagingRecord messagingRecord;
}
