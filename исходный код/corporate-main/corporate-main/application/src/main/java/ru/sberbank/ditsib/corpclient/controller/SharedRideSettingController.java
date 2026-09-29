package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.corpclient.dto.SharedRideSettingsCreateDTO;
import ru.sberbank.ditsib.corpclient.dto.SharedRideSettingsUpdateDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.UUID;

@RequestMapping({"/{organizationId}/shared_ride_settings", "/{organizationId}/shared_ride_settings/"})
@Validated
@Tag(
        name = "Настройки совместных поездок",
        description = "Набор операций для настройки совместных поездок для конкретной организации"
)
public interface SharedRideSettingController {
    
    /**
     * Получение всех настроек
     * @return все настройки
     */
    @Operation(
            summary = "Получение всех настроек",
            description = "Получение всех настроек совместных поездок для конкретной организации"
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody Collection<SharedRideSettingsUpdateDTO> getAll(
            @PathVariable("organizationId") UUID organizationId
    );
    
    /**
     * Получение конкретной настройки
     * @param organizationId идентификатор организации
     * @param id идентификатор настройки
     * @return настройка
     */
    @Operation(
            summary = "Получение настройки",
            description = "Получение настройки совместных поездок для конкретной организации"
    )
    @GetMapping(value = {"{id}", "{id}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody SharedRideSettingsUpdateDTO get(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("id") @NotNull UUID id
    );
    
    /**
     * Создание новой настройки
     * @param organizationId идентификатор организации
     * @param newSettingsDto данные новой настройки
     * @return записанная в БД настройка
     */
    @Operation(
            summary = "Создание новой настройки",
            description = "Добавление новой настройки совместных поездок для конкретной организации")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody SharedRideSettingsUpdateDTO add(
            @PathVariable("organizationId") UUID organizationId,
            @RequestBody @Valid SharedRideSettingsCreateDTO newSettingsDto
    );
    
    /**
     * Изменение настройки
     * @param organizationId идентификатор организации
     * @param updatedSettingDto измененные данные настройки
     */
    @Operation(
            summary = "Изменение настройки",
            description = "Изменение настройки совместных поездок для конкретной организации"
    )
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody SharedRideSettingsUpdateDTO edit(
            @PathVariable("organizationId") UUID organizationId,
            @RequestBody @Valid SharedRideSettingsUpdateDTO updatedSettingDto
    );
    
    /**
     * Удаление настройки
     * @param organizationId идентификатор организации
     * @param settingsId идентификатор настройки
     */
    @Operation(
            summary = "Удаление настройки",
            description = "Удаление настройки совместных поездок для конкретной организации"
    )
    @DeleteMapping({"{id}", "{id}/"})
    void delete(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("id") @NotNull UUID settingsId
    );
}
