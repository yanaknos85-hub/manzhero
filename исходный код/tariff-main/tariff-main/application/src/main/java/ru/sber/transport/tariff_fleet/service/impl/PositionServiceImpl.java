package ru.sber.transport.tariff_fleet.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.tariff_fleet.database.dao.PositionRepository;
import ru.sber.transport.tariff_fleet.database.model.Position;
import ru.sber.transport.tariff_fleet.service.PositionService;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of position service.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class PositionServiceImpl implements PositionService {

    private final PositionRepository repository;

    @Override
    public Optional<Position> get(UUID id) {
        return repository.findById(id);
    }

    @Override
    public void delete(Position entity) {
        repository.save(entity
                .setActive(false));
    }
    
    @Override
    public void saveOrUpdate(Position entity) {
        var dbEntityOptional = repository.findById(entity.getId());
        if (dbEntityOptional.isPresent()) {
            repository.save(dbEntityOptional.get()
                                            .setActive(true)
                                            .setOrganizationId(entity.getOrganizationId())
                                            .setPositionName(entity.getPositionName()));
        } else {
            repository.save(entity);
        }
    }
}
