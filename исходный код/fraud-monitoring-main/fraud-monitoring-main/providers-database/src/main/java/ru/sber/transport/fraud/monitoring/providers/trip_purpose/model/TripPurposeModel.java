package ru.sber.transport.fraud.monitoring.providers.trip_purpose.model;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.database.fraud_monitoring.tables.records.TripPurposeRecord;
import ru.sber.transport.fraud.monitoring.model.TripPurpose;

/**
 * Реализация модели цели поездки
 */
@RequiredArgsConstructor
public class TripPurposeModel implements TripPurpose {

    @Delegate
    private final TripPurposeRecord source;
}