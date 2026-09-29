package ru.sberbank.ditsib.transport.approvals.controller.settings.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sberbank.ditsib.transport.approvals.controller.settings.PublicTrApprovalsSettingsController;
import ru.sberbank.ditsib.transport.approvals.database.model.PublicTrApprovalsSettings;
import ru.sberbank.ditsib.transport.approvals.dto.settings.NewPublicTrApprovalsSettingsDTO;
import ru.sberbank.ditsib.transport.approvals.dto.settings.PublicTrApprovalsSettingsDTO;
import ru.sberbank.ditsib.transport.approvals.mappers.ApprovalsSettingsMapper;
import ru.sberbank.ditsib.transport.approvals.services.ApprovalsSettingsService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;

import java.util.UUID;

@E2EController
@RestController
@RequiredArgsConstructor
public class PublicApprovalsSettingsControllerImpl implements PublicTrApprovalsSettingsController {
    
    private final ApprovalsSettingsService<PublicTrApprovalsSettings> settingsService;
    
    private final ApprovalsSettingsMapper mapper;
    
    @Override
    @CheckOrganizationAccess
    public PublicTrApprovalsSettingsDTO add(@Organization UUID organizationId, NewPublicTrApprovalsSettingsDTO newRequest) {
        return mapper.toDto(settingsService.add(organizationId, mapper.toModel(newRequest)));
    }
    
    @Override
    @CheckOrganizationAccess
    public PublicTrApprovalsSettingsDTO get(@Organization UUID organizationId) {
        return mapper.toDto(settingsService.get(organizationId));
    }
    
    @Override
    @CheckOrganizationAccess
    public void update(@Organization UUID organizationId, UUID settingId, NewPublicTrApprovalsSettingsDTO newRequest
                                           ) {
        settingsService.update(organizationId, settingId, mapper.toModel(newRequest));
    }
    
    @Override
    @CheckOrganizationAccess
    public void delete(@Organization UUID organizationId, UUID settingId) {
        settingsService.delete(organizationId, settingId);
    }
    
    @Override
    @CheckOrganizationAccess
    public PublicTrApprovalsSettingsDTO restoreValues(@Organization UUID organizationId, UUID settingId) {
        return mapper.toDto(settingsService.restoreValues(organizationId, settingId));
    }
}