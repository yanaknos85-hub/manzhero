package ru.sberbank.ditsib.transport.limits.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.limits.constants.RemainsTransferTarget;
import ru.sberbank.ditsib.transport.limits.constants.SettingsNames;
import ru.sberbank.ditsib.transport.limits.dto.LimitColorsDTO;
import ru.sberbank.ditsib.transport.limits.dto.LimitSettingsDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;

/**
 * Controller interface for limit settingss.
 */
@RequestMapping(value = "/limitsettings")
@Tag(name = "Настройки лимита", description = "Настройки лимита")
public interface LimitSettingsController {
    
    /**
     * Add a new limit settings.
     *
     * @param limitSettingsDTO new limit settings data.
     *
     * @return added limit.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Сохранение значения настройки", description = "Сохранение значения настройки")
    LimitSettingsDTO save(@Valid @RequestBody LimitSettingsDTO limitSettingsDTO);
    
    /**
     * Add limit settings for EMP_LIMIT_REMAINS_TARGET and DEP_LIMIT_REMAINS_TARGET.
     *
     * @param emptarget emp target.
     * @param deptarget dep target.
     */
    @PostMapping(value = "/target/emp/{emptarget}/dep/{deptarget}")
    @ResponseBody
    @Operation(summary = "Сохранение значения настроек",
               description = "Сохранение значения настроек EMP_LIMIT_REMAINS_TARGET and DEP_LIMIT_REMAINS_TARGET")
    void saveTarget(@PathVariable("emptarget") RemainsTransferTarget emptarget,
                    @PathVariable("deptarget") RemainsTransferTarget deptarget);
    
    /**
     * Add limit settings for limit colors
     *
     * @param limitColorsDTO params.
     */
    @PostMapping(value = "/boundaries")
    @ResponseBody
    @Operation(summary = "Сохранение значения настроек",
               description = "Сохранение значения настроек цветового отображения лимитов")
    void saveColors(@Valid @RequestBody LimitColorsDTO limitColorsDTO);
    
    /**
     * Delete limit settings.
     *
     * @param limitSettingsName name of limit sharing to delete.
     */
    @DeleteMapping(value = "{limitSettingsName}")
    @Operation(summary = "Удаление настройки", description = "Удаление настройки")
    void delete(@PathVariable("limitSettingsName") SettingsNames limitSettingsName);
    
    /**
     * Get limit settings with ID.
     *
     * @param limitSettingsName ID of limit settings to get.
     *
     * @return limit settings.
     */
    @GetMapping(value = "{limitSettingsName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение настройки", description = "Получение настройки")
    LimitSettingsDTO get(@PathVariable("limitSettingsName") @NotNull SettingsNames limitSettingsName);
    
    /**
     * Get limit settings with ID.
     *
     * @param limitSettingsName ID of limit settings to get.
     *
     * @return limit settings.
     */
    @GetMapping(value = "/getByName/{limitSettingsName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение настройки", description = "Получение настройки")
    String getByName(@PathVariable("limitSettingsName") @NotNull SettingsNames limitSettingsName);
    
    /**
     * Get all limit settingss.
     *
     * @return list of limit settingss.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех настроек", description = "Получение всех настроек")
    Collection<LimitSettingsDTO> getAll();
}
