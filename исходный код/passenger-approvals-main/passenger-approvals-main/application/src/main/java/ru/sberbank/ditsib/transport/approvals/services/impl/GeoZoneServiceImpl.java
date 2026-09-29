package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.approvals.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.PurposeAndRegionApprovalSettingsItem;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.approvals.services.GeoZoneService;

import java.util.List;
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
    public List<PurposeAndRegionApprovalSettingsItem> setAllGeoZonesToSettingsItems(
            List<PurposeAndRegionApprovalSettingsItem> settingsItems) {
        for (var item : settingsItems) {
            if (item.getRegion().getId() != null) {
                item.setRegion(get(item.getRegion().getId()).orElseThrow(
                        () -> new EntityNotFoundException(GeoZone.class, item.getRegion().getId())));
            } else {
                item.setRegion(null);
            }
        }
        return settingsItems;
    }
}
