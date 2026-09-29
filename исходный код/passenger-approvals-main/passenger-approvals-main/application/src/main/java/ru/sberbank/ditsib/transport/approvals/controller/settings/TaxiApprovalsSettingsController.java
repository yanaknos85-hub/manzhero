package ru.sberbank.ditsib.transport.approvals.controller.settings;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.approvals.dto.settings.NewTaxiApprovalsSettingsDTO;
import ru.sberbank.ditsib.transport.approvals.dto.settings.TaxiApprovalsSettingsDTO;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * Контроллер для работы с настройками согласований заявок на такси для корп.клиента
 **/
@RequestMapping({"{organizationId}/settings/request/taxi", "{organizationId}/settings/request/taxi/"})
@Tag(name = "Настройки согласований заявок на такси", description = "Модуль управления согласованиями")
public interface TaxiApprovalsSettingsController {
    
    /**
     * Добавить настройку согласования заявки на поездку на такси
     * @return новая настройка согласования заявки на поездку на такси
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление",
               description = "Добавление настройки согласований заявок на такси")
    TaxiApprovalsSettingsDTO add(
            @PathVariable("organizationId") UUID organizationId,
            @RequestBody @Valid NewTaxiApprovalsSettingsDTO newRequest);
    
    /**
     * Получить настройки согласования заявки на поездку на такси
     * @return настройки согласования заявки на поездку на такси
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получить настройки согласований заявок на такси",
               description = "Получить настройки согласований заявок на такси")
    TaxiApprovalsSettingsDTO get(@PathVariable("organizationId") UUID organizationId);

    /**
     * Обновить настройки согласования заявки на поездку на такси
     */
    @PutMapping(path = "/{settingId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Обновить настройки согласований заявок на такси",
               description = "Обновить настройки согласований заявок на такси")
    void update(@PathVariable("organizationId") UUID organizationId,
                @PathVariable("settingId") UUID settingId,
                @RequestBody @Valid NewTaxiApprovalsSettingsDTO newRequest);

    /**
     * Удалить настройку согласования заявки на поездку на такси
     */
    @DeleteMapping("/{settingId}")
    @ResponseBody
    @Operation(summary = "Удалить настройку согласования заявки на поездку на такси",
               description = "Удалить настройку согласования заявки на поездку на такси")
    void delete(@PathVariable("organizationId") UUID organizationId,
                @PathVariable("settingId") UUID settingId);
    
    /**
     * Сбросить настройку согласования заявки на значение по умолчанию
     * @return сброшенная настройка согласования заявки
     */
    @PostMapping("/{settingId}/restore")
    @ResponseBody
    @Operation(summary = "Сбросить настройку согласования заявки на значение по умолчанию",
               description = "Сбросить настройку согласования заявки на значение по умолчанию")
    TaxiApprovalsSettingsDTO restoreValues(@PathVariable("organizationId") UUID organizationId,
                                           @PathVariable("settingId") UUID settingId);
}
