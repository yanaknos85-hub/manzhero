package ru.sberbank.ditsib.transport.approvals.controller.settings.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sberbank.ditsib.transport.approvals.controller.settings.OtherTrTypesApprovalsSettingsController;
import ru.sberbank.ditsib.transport.approvals.dto.settings.NewOtherTrTypesApprovalsSettingsDTO;
import ru.sberbank.ditsib.transport.approvals.dto.settings.OtherTrTypesApprovalsSettingsDTO;
import ru.sberbank.ditsib.transport.approvals.mappers.ApprovalsSettingsMapper;
import ru.sberbank.ditsib.transport.approvals.services.OtherTrTypesApprovalsSettingsService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;

import java.util.UUID;

@E2EController
@RestController
@RequiredArgsConstructor
public class OtherTrTypesApprovalsSettingsControllerImpl implements OtherTrTypesApprovalsSettingsController {
    
    private final OtherTrTypesApprovalsSettingsService settingsService;
    
    private final ApprovalsSettingsMapper mapper;
    
    @Override
    @CheckOrganizationAccess
    public OtherTrTypesApprovalsSettingsDTO add(@Organization UUID organizationId, NewOtherTrTypesApprovalsSettingsDTO newRequest) {
        return mapper.toDto(settingsService.add(organizationId, mapper.toModel(newRequest)));
    }
    
    @Override
    @CheckOrganizationAccess
    public OtherTrTypesApprovalsSettingsDTO get(@Organization UUID organizationId, String transportType) {
        return mapper.toDto(settingsService.get(organizationId, transportType));
    }
    
    @Override
    @CheckOrganizationAccess
    public void update(@Organization UUID organizationId, String transportType, NewOtherTrTypesApprovalsSettingsDTO newRequest) {
        settingsService.update(organizationId, transportType, mapper.toModel(newRequest));
    }
    
    @Override
    @CheckOrganizationAccess
    public void delete(@Organization UUID organizationId, String transportType) {
        settingsService.delete(organizationId, transportType);
    }
    
    @Override
    @CheckOrganizationAccess
    public OtherTrTypesApprovalsSettingsDTO restoreValues(@Organization UUID organizationId,
                                                          String transportType) {
        var restored = settingsService.restoreValues(organizationId, transportType);
        return mapper.toDto(restored);
    }
}