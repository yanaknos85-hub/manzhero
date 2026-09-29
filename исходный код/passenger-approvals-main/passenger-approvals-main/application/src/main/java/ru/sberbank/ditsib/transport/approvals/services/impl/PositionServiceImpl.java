package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.approvals.database.dao.PositionRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.Position;
import ru.sberbank.ditsib.transport.approvals.exception.AwaitingSynchronizationException;
import ru.sberbank.ditsib.transport.approvals.services.PositionService;
import ru.sberbank.ditsib.transport.approvals.services.grpc.Positions;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of position service.
 */
@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class PositionServiceImpl implements PositionService {
    
    private final PositionRepository repository;
    private final Positions positions;
    
    @Override
    public Optional<Position> get(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    public void delete(Position entity) {
        repository.save(entity.setActive(false));
    }
    
    @Override
    public Position save(Position entity) {
        return repository.save(entity);
    }

    @SneakyThrows
    @Override
    public void saveGrpcEntity(String message, UUID id) {
        this.save(Optional.ofNullable(positions.one(id)).orElseThrow(() -> {
            log.info(message);
            throw new AwaitingSynchronizationException("Awaiting an position synchronization");
        }));
    }
}
