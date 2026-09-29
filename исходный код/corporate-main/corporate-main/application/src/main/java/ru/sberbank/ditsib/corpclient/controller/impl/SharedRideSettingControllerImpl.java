package ru.sberbank.ditsib.corpclient.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sberbank.ditsib.corpclient.controller.SharedRideSettingController;
import ru.sberbank.ditsib.corpclient.dto.SharedRideSettingsCreateDTO;
import ru.sberbank.ditsib.corpclient.dto.SharedRideSettingsUpdateDTO;
import ru.sberbank.ditsib.corpclient.service.OrganizationService;
import ru.sberbank.ditsib.corpclient.service.SharedRideSettingService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class SharedRideSettingControllerImpl implements SharedRideSettingController {
    
    private final SharedRideSettingService settingsService;
    
    private final OrganizationService organizationService;
    
    @CheckOrganizationAccess
    @Override
    public List<SharedRideSettingsUpdateDTO> getAll(@Organization UUID organizationId) {
        organizationService.validateOrganizationId(organizationId);
        return settingsService.getAllSettingsByOrganizationId(organizationId);
    }
    
    @CheckOrganizationAccess
    @Override
    public SharedRideSettingsUpdateDTO get(@Organization UUID organizationId, UUID id) {
        organizationService.validateOrganizationId(organizationId);
        return settingsService.getSingleSettingsByOrganizationIdAndSettingId(organizationId, id);
    }
    
    @CheckOrganizationAccess
    @Override
    public SharedRideSettingsUpdateDTO add(
            @Organization UUID organizationId,
            SharedRideSettingsCreateDTO newSettingsDto
                                              ) {
        organizationService.validateOrganizationId(organizationId);
        return settingsService.saveNewSettings(organizationId, newSettingsDto);
    }
    
    @CheckOrganizationAccess
    @Override
    public SharedRideSettingsUpdateDTO edit(
            @Organization UUID organizationId,
            SharedRideSettingsUpdateDTO updatedSettingDto
    ) {
        organizationService.validateOrganizationId(organizationId);
        return settingsService.updateSettings(organizationId, updatedSettingDto);
    }
    
    @CheckOrganizationAccess
    @Override
    public void delete(@Organization UUID organizationId, UUID settingsId) {
        organizationService.validateOrganizationId(organizationId);
        settingsService.deleteSettings(organizationId, settingsId);
    }
}
