package ru.sberbank.ditsib.corpclient.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.corpclient.controller.SharedRideSettingsTypeController;
import ru.sberbank.ditsib.corpclient.database.model.SharedRideSettingType;
import ru.sberbank.ditsib.corpclient.service.SharedRideSettingsTypeService;

import java.util.Set;

@RequiredArgsConstructor
@RestController
public class SharedRideSettingsTypeControllerImpl implements SharedRideSettingsTypeController {
    
    private final SharedRideSettingsTypeService settingsService;
    
    @Override
    public Set<SharedRideSettingType> getAll() {
        return settingsService.getAllSettingsTypes();
    }
}