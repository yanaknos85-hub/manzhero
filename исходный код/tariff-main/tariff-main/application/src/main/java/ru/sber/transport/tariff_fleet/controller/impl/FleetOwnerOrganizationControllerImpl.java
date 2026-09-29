package ru.sber.transport.tariff_fleet.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.tariff_fleet.controller.FleetOwnerOrganizationController;
import ru.sber.transport.tariff_fleet.dto.GetAllActiveOrganizationNamesDto;
import ru.sber.transport.tariff_fleet.service.FleetOwnerOrganizationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FleetOwnerOrganizationControllerImpl implements FleetOwnerOrganizationController {
    private final FleetOwnerOrganizationService fleetOwnerOrganizationService;
    
    @Override
    public List<GetAllActiveOrganizationNamesDto> getFleetOwnerOrganizations() {
        return fleetOwnerOrganizationService.getAllActive();
    }
}
