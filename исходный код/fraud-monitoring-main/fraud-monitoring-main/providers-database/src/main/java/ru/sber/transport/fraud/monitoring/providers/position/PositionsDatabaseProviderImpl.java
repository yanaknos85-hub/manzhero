package ru.sber.transport.fraud.monitoring.providers.position;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.fraud_monitoring.Tables;
import ru.sber.transport.database.fraud_monitoring.tables.records.PositionRecord;
import ru.sber.transport.fraud.monitoring.model.Position;
import ru.sber.transport.fraud.monitoring.providers.PositionsDatabaseProvider;

import java.util.UUID;

/**
 * Реализация провайдера должностей
 */
@RequiredArgsConstructor
public class PositionsDatabaseProviderImpl implements PositionsDatabaseProvider, JooqRepository<ru.sber.transport.database.fraud_monitoring.tables.Position, PositionRecord, UUID> {

    @Override
    public ru.sber.transport.database.fraud_monitoring.tables.Position table() {
        return Tables.POSITION;
    }

    @Override
    public Position createOrUpdate(Position source) {
        if (source == null) {
            return null;
        }
        final var positionRecord = findById(source.getId()).orElseGet(PositionRecord::new);
        positionRecord.setId(source.getId());
        positionRecord.setName(source.getName());

        final var saved = save(positionRecord);

        return createPosition(saved);
    }

    @Override
    public Position get(UUID id) {
        return findById(id)
                .map(this::createPosition)
                .orElse(null);

    }

    private Position createPosition(@NotNull PositionRecord source) {
        return new Position() {
            @Override
            public UUID getId() {
                return source.getId();
            }

            @Override
            public String getName() {
                return source.getName();
            }
        };
    }
}
