package ru.sber.transport.fraud.monitoring.providers.trip_purpose;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.fraud_monitoring.Tables;
import ru.sber.transport.database.fraud_monitoring.tables.records.TripPurposeRecord;
import ru.sber.transport.fraud.monitoring.model.TripPurpose;
import ru.sber.transport.fraud.monitoring.providers.TripPurposesDatabaseProvider;

import java.util.UUID;


/**
 * Реализация провайдера целей поездок.
 */
@Slf4j
@Transactional
@RequiredArgsConstructor
public class TripPurposesDatabaseProviderImpl implements TripPurposesDatabaseProvider, JooqRepository<ru.sber.transport.database.fraud_monitoring.tables.TripPurpose, TripPurposeRecord, UUID> {

    @Override
    public ru.sber.transport.database.fraud_monitoring.tables.TripPurpose table() {
        return Tables.TRIP_PURPOSE;
    }

    @Override
    public TripPurpose createOrUpdate(TripPurpose source) {
        final var item = findById(source.getId()).orElseGet(TripPurposeRecord::new);
        item.setId(source.getId());
        item.setLabel(source.getLabel());

        final var saved = save(item);

        return createTripPurpose(saved);
    }

    @Override
    public TripPurpose get(UUID id) {
        return findById(id).map(this::createTripPurpose)
                .map(it -> {
                    log.info("Trip purpose {} found", id);
                    return it;
                })
                .orElse(null);
    }

    @NotNull
    private TripPurpose createTripPurpose(TripPurposeRecord source) {
        return new TripPurpose() {

            @Override
            public UUID getId() {
                return source.getId();
            }

            @Override
            public String getLabel() {
                return source.getLabel();
            }
        };
    }
}
