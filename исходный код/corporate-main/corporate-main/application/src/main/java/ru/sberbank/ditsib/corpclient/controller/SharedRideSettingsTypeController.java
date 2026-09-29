package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sberbank.ditsib.corpclient.database.model.SharedRideSettingType;

import java.util.Set;

@RequestMapping({"/shared_ride_settings_types", "/shared_ride_settings_types/"})
@Validated
@Tag(
        name = "Типы настроек совместных поездок",
        description = "Операция получения типов настроек совместных поездок"
)
public interface SharedRideSettingsTypeController {
    
    /**
     * Получение всех типов настроек совместных поездок.
     * Настройки общие для любой организации, ее ID не проверяется
     * @return все типы настроек
     */
    @Operation(
            summary = "Получение всех типов настроек совместных поездок",
            description = "Получение всех типов настроек совместных поездок"
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Set<SharedRideSettingType> getAll();
}
