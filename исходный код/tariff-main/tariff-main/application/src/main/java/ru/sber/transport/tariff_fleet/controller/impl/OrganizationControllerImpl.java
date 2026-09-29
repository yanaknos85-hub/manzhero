package ru.sber.transport.tariff_fleet.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.tariff_fleet.controller.OrganizationController;
import ru.sber.transport.tariff_fleet.dto.GetAllActiveOrganizationNamesDto;
import ru.sber.transport.tariff_fleet.dto.GetDepartmentsInfo;
import ru.sber.transport.tariff_fleet.dto.OrganizationsDepartmentsSearchDto;
import ru.sber.transport.tariff_fleet.service.OrganizationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrganizationControllerImpl implements OrganizationController {
    
    private final OrganizationService organizationService;
    
    @Override
    public List<GetAllActiveOrganizationNamesDto> getAllActiveNames() {
        return organizationService.getAllActiveNames();
    }

    @Override
    public List<GetDepartmentsInfo> getOrganizationsDepartments(OrganizationsDepartmentsSearchDto dto) {
        return organizationService.getDepartmentsInfo(dto);
    }
}
