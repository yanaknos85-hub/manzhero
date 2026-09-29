package ru.sberbank.ditsib.corpclient.service.impl;

import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.corpclient.database.model.SharedRideSettingType;
import ru.sberbank.ditsib.corpclient.service.SharedRideSettingsTypeService;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SharedRideSettingsTypeServiceImpl implements SharedRideSettingsTypeService {
    
    @Override
    public Set<SharedRideSettingType> getAllSettingsTypes() {
        return Arrays.stream(SharedRideSettingType.values()).collect(Collectors.toSet());
    }
}