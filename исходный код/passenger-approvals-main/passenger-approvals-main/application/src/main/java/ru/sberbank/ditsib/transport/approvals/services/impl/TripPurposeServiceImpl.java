package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.approvals.database.dao.TripPurposeRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.PurposeAndRegionApprovalSettingsItem;
import ru.sberbank.ditsib.transport.approvals.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.approvals.services.TripPurposeService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of purpose service.
 */
@RequiredArgsConstructor
@Service
@Transactional
public class TripPurposeServiceImpl implements TripPurposeService {
    
    private final TripPurposeRepository repository;
    
    @Override
    public void save(TripPurpose purpose) {
        repository.save(purpose);
    }
    
    @Override
    public List<TripPurpose> getByIds(Set<UUID> idList) {
        return repository.findAllById(idList);
    }

    @Override
    public List<PurposeAndRegionApprovalSettingsItem> setAllPurposesToSettingsItems(
            List<PurposeAndRegionApprovalSettingsItem> settingsItems) {
        settingsItems.forEach(item -> item.setTripPurpose(get(item.getTripPurpose().getId())));
        return settingsItems;
    }
    
    @Override
    public TripPurpose get(UUID id) {
        return repository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(TripPurpose.class, id));
    }
}
