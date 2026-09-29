package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.corpclient.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.corpclient.database.model.messages.GeoZone;
import ru.sberbank.ditsib.corpclient.service.GeoZoneService;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса для работы с геозонами.
 */
@RequiredArgsConstructor
@Component
class GeoZoneServiceImpl implements GeoZoneService {
    
    private final GeoZoneRepository repository;
    
    @Override
    public Optional<GeoZone> get(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    public void delete(GeoZone geoZone) {
        if (repository.existsById(geoZone.getId())) {
            repository.delete(geoZone);
        }
    }
    
    @Override
    public void save(GeoZone geoZone) {
        repository.save(geoZone);
    }
}
