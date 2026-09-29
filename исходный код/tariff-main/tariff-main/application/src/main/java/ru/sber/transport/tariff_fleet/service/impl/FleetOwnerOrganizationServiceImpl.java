package ru.sber.transport.tariff_fleet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.tariff_fleet.database.dao.FleetOwnerOrganizationRepository;
import ru.sber.transport.tariff_fleet.dto.GetAllActiveOrganizationNamesDto;
import ru.sber.transport.tariff_fleet.service.FleetOwnerOrganizationService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FleetOwnerOrganizationServiceImpl implements FleetOwnerOrganizationService {
    
    private final FleetOwnerOrganizationRepository fleetOwnerOrganizationRepository;
    
    @Override
    public boolean existsById(UUID id) {
        return fleetOwnerOrganizationRepository.existsById(id);
    }

    @Override
    public List<GetAllActiveOrganizationNamesDto> getAllActive() {
        return fleetOwnerOrganizationRepository.findAllActive();
    }
}
