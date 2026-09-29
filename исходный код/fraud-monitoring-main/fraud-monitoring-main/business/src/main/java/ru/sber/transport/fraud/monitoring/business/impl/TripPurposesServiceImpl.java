package ru.sber.transport.fraud.monitoring.business.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.fraud.monitoring.business.TripPurposesService;
import ru.sber.transport.fraud.monitoring.model.TripPurpose;
import ru.sber.transport.fraud.monitoring.providers.TripPurposesDatabaseProvider;
import ru.sber.transport.fraud.monitoring.providers.TripPurposesGrpcProvider;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class TripPurposesServiceImpl implements TripPurposesService {

    private final TripPurposesDatabaseProvider tripPurposesDatabaseProvider;
    private final TripPurposesGrpcProvider tripPurposesGrpcProvider;

    @Override
    public TripPurpose createOrUpdate(TripPurpose source) {
        return tripPurposesDatabaseProvider.createOrUpdate(source);
    }

    @Override
    public TripPurpose getExistedOrCreate(UUID id) {
        final var tripPurpose = tripPurposesDatabaseProvider.get(id);
        if (tripPurpose != null) {
            return tripPurpose;
        }

        final var requestedTripPurpose = tripPurposesGrpcProvider.get(id);
        final var saved = createOrUpdate(requestedTripPurpose);
        log.info("Responded trip purpose {} saved", id);
        return saved;
    }
}
