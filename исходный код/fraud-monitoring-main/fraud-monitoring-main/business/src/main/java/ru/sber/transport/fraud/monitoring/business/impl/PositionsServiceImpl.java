package ru.sber.transport.fraud.monitoring.business.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.fraud.monitoring.business.PositionsService;
import ru.sber.transport.fraud.monitoring.model.Position;
import ru.sber.transport.fraud.monitoring.providers.PositionsDatabaseProvider;
import ru.sber.transport.fraud.monitoring.providers.PositionsGrpcProvider;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class PositionsServiceImpl implements PositionsService {

    private final PositionsDatabaseProvider positionsDatabaseProvider;
    private final PositionsGrpcProvider positionsGrpcProvider;

    @Override
    public Position createOrUpdate(Position source) {
        return positionsDatabaseProvider.createOrUpdate(source);
    }

    @Override
    public Position getExistedOrCreate(UUID id) {
        final var position = positionsDatabaseProvider.get(id);
        if (position != null) {
            return position;
        }

        final var requestedPosition = positionsGrpcProvider.get(id);
        final var saved = createOrUpdate(requestedPosition);
        log.info("Responded position {} saved", id);
        return saved;
    }
}
