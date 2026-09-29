package ru.sber.transport.corporate.providers.trip_purpose;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.corporate.business.model.TripPurpose;
import ru.sber.transport.corporate.business.providers.TripPurposeProvider;
import ru.sber.transport.database.corporate.tables.records.TripPurposeRecord;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TripPurposeProviderImpl implements TripPurposeProvider, JooqRepository<ru.sber.transport.database.corporate.tables.TripPurpose, TripPurposeRecord, UUID> {

    private final TripPurposeDatabaseMapper mapper;

    @Override
    public ru.sber.transport.database.corporate.tables.TripPurpose table() {
        return ru.sber.transport.database.corporate.tables.TripPurpose.TRIP_PURPOSE;
    }

    @Override
    public Optional<TripPurpose> get(UUID id) {
        return findById(id).map(mapper::toBusiness);
    }

}
