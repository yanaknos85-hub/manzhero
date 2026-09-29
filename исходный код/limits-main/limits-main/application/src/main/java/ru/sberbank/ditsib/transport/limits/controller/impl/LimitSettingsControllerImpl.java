package ru.sberbank.ditsib.transport.limits.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.limits.constants.RemainsTransferTarget;
import ru.sberbank.ditsib.transport.limits.constants.SettingsNames;
import ru.sberbank.ditsib.transport.limits.controller.LimitSettingsController;
import ru.sberbank.ditsib.transport.limits.dto.LimitColorsDTO;
import ru.sberbank.ditsib.transport.limits.dto.LimitSettingsDTO;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSettings;
import ru.sberbank.ditsib.transport.limits.service.LimitSettingsService;

import java.util.List;

/**
 * Implementation of limit controller service.
 */
@RequiredArgsConstructor
@RestController
@Scope("request")
class LimitSettingsControllerImpl implements LimitSettingsController {
    
    private final LimitSettingsService limitSettingsService;
    
    @Override
    public LimitSettingsDTO save(LimitSettingsDTO limitSettingsDTO) {
       
        LimitSettings limitSettings = new LimitSettings();
        limitSettings.setName(limitSettingsDTO.getName());
        limitSettings.setValue(limitSettingsDTO.getValue());
        limitSettings = limitSettingsService.save(limitSettings);
        
        return transformEntityToDTO(limitSettings);
    }
    
    @Override
    public void saveTarget(RemainsTransferTarget emptarget, RemainsTransferTarget deptarget) {
        limitSettingsService.add(SettingsNames.EMP_LIMIT_REMAINS_TARGET, emptarget.name());
        limitSettingsService.add(SettingsNames.DEP_LIMIT_REMAINS_TARGET, deptarget.name());
    }
    
    @Override
    public void saveColors(LimitColorsDTO dto) {
        limitSettingsService.add(SettingsNames.EMP_LIMIT_GREEN_FROM, dto.getEmpLimitGreenFrom());
        limitSettingsService.add(SettingsNames.EMP_LIMIT_GREEN_UNTIL, dto.getEmpLimitGreenUntil());
    
        limitSettingsService.add(SettingsNames.EMP_LIMIT_YELLOW_FROM, dto.getEmpLimitYellowFrom());
        limitSettingsService.add(SettingsNames.EMP_LIMIT_YELLOW_UNTIL, dto.getEmpLimitYellowUntil());
    
        limitSettingsService.add(SettingsNames.EMP_LIMIT_RED_FROM, dto.getEmpLimitRedFrom());
        limitSettingsService.add(SettingsNames.EMP_LIMIT_RED_UNTIL, dto.getEmpLimitRedUntil());
    
        limitSettingsService.add(SettingsNames.DEP_LIMIT_GREEN_FROM, dto.getDepLimitGreenFrom());
        limitSettingsService.add(SettingsNames.DEP_LIMIT_GREEN_UNTIL, dto.getDepLimitGreenUntil());
    
        limitSettingsService.add(SettingsNames.DEP_LIMIT_YELLOW_FROM, dto.getDepLimitYellowFrom());
        limitSettingsService.add(SettingsNames.DEP_LIMIT_YELLOW_UNTIL, dto.getDepLimitYellowUntil());
    
        limitSettingsService.add(SettingsNames.DEP_LIMIT_RED_FROM, dto.getDepLimitRedFrom());
        limitSettingsService.add(SettingsNames.DEP_LIMIT_RED_UNTIL, dto.getDepLimitRedUntil());
    }
    
    @Override
    public void delete(SettingsNames limitSettingsName) {
        limitSettingsService.delete(limitSettingsName);
    }
    
    @Override
    public LimitSettingsDTO get(SettingsNames limitSettingsName) {
        LimitSettings limitSettings = limitSettingsService.get(limitSettingsName);
        if (limitSettings == null) {
            throw new EntityNotFoundException(LimitSettings.class, limitSettingsName.name());
        }
        return transformEntityToDTO(limitSettings);
    }
    
    @Override
    public String getByName(SettingsNames limitSettingsName) {
        return limitSettingsService.getByName(limitSettingsName);
    }
    
    @Override
    public List<LimitSettingsDTO> getAll() {
        List<LimitSettings> list = limitSettingsService.getAll();
        return list.stream().map(this::transformEntityToDTO).toList();
    }
    
    private LimitSettingsDTO transformEntityToDTO(LimitSettings limitSettings) {
        if (limitSettings == null) {
            return null;
        }
        LimitSettingsDTO limitSettingsDTO = new LimitSettingsDTO();
        limitSettingsDTO.setName(limitSettings.getName());
        limitSettingsDTO.setValue(limitSettings.getValue());
        return limitSettingsDTO;
    }
}