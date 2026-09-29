package ru.sberbank.ditsib.transport.approvals.controller.settings.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sberbank.ditsib.transport.approvals.controller.settings.TaxiApprovalsSettingsController;
import ru.sberbank.ditsib.transport.approvals.database.model.TaxiApprovalsSettings;
import ru.sberbank.ditsib.transport.approvals.dto.settings.NewTaxiApprovalsSettingsDTO;
import ru.sberbank.ditsib.transport.approvals.dto.settings.TaxiApprovalsSettingsDTO;
import ru.sberbank.ditsib.transport.approvals.mappers.ApprovalsSettingsMapper;
import ru.sberbank.ditsib.transport.approvals.services.ApprovalsSettingsService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;

import java.util.UUID;

@E2EController
@RestController
@RequiredArgsConstructor
public class TaxiApprovalsSettingsControllerImpl implements TaxiApprovalsSettingsController {
    
    private final ApprovalsSettingsService<TaxiApprovalsSettings> settingsService;
    
    private final ApprovalsSettingsMapper mapper;
    
    @Override
    @CheckOrganizationAccess
    public TaxiApprovalsSettingsDTO add(@Organization UUID organizationId, NewTaxiApprovalsSettingsDTO newRequest) {
        return mapper.toDto(settingsService.add(organizationId, mapper.toModel(newRequest)));
    }
    
    @Override
    @CheckOrganizationAccess
    public TaxiApprovalsSettingsDTO get(@Organization UUID organizationId) {
        return mapper.toDto(settingsService.get(organizationId));
    }
    
    @Override
    @CheckOrganizationAccess
    public void update(@Organization UUID organizationId, UUID settingId, NewTaxiApprovalsSettingsDTO newRequest) {
        settingsService.update(organizationId, settingId, mapper.toModel(newRequest));
    }
    
    @Override
    @CheckOrganizationAccess
    public void delete(@Organization UUID organizationId, UUID settingId) {
        settingsService.delete(organizationId, settingId);
    }
    
    @Override
    @CheckOrganizationAccess
    public TaxiApprovalsSettingsDTO restoreValues(@Organization UUID organizationId, UUID settingId) {
        return mapper.toDto(settingsService.restoreValues(organizationId, settingId));
    }
}
