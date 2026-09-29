package ru.sberbank.ditsib.transport.approvals.controller.settings;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.approvals.dto.settings.NewPublicTrApprovalsSettingsDTO;
import ru.sberbank.ditsib.transport.approvals.dto.settings.PublicTrApprovalsSettingsDTO;

import jakarta.validation.Valid;
import java.util.UUID;
/**
 * Контроллер для работы с настройками согласований заявок на поездку на общественном транспорте для корп.клиента
 **/
@RequestMapping({"{organizationId}/settings/request/public", "{organizationId}/settings/request/public/"})
@Tag(name = "Настройки согласований заявок на поездку на общественном транспорте",
     description = "Модуль управления согласованиями")
public interface PublicTrApprovalsSettingsController {
    
    /**
     * Добавить настройку согласования заявки на поездку на общественном транспорте
     * @return новая настройка согласования заявки на поездку на общественном транспорте
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление",
               description = "Добавление настройки согласований заявок на поездку на общественном транспорте")
    PublicTrApprovalsSettingsDTO add(
            @PathVariable("organizationId") UUID organizationId,
            @RequestBody @Valid NewPublicTrApprovalsSettingsDTO newRequest);
    
    /**
     * Получить настройки согласования заявки на поездку на общественном транспорте
     * @return настройки согласования заявки на поездку на общественном транспорте
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получить настройки согласований заявок на поездку на общественном транспорте",
               description = "Получить настройки согласований заявок на поездку на общественном транспорте")
    PublicTrApprovalsSettingsDTO get(@PathVariable("organizationId") UUID organizationId);
    
    /**
     * Обновить настройки согласования заявки на поездку на общественном транспорте
     */
    @PutMapping(path = "/{settingId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Обновить настройки согласований заявок на поездку на общественном транспорте",
               description = "Обновить настройки согласований заявок на поездку на общественном транспорте")
    void update(@PathVariable("organizationId") UUID organizationId,
                @PathVariable("settingId") UUID settingId,
                @RequestBody @Valid NewPublicTrApprovalsSettingsDTO newRequest);
    
    /**
     * Удалить настройку согласования заявки на поездку на общественном транспорте
     */
    @DeleteMapping("/{settingId}")
    @ResponseBody
    @Operation(summary = "Удалить настройку согласования заявки на поездку на общественном транспорте",
               description = "Удалить настройку согласования заявки на поездку на общественном транспорте")
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
    PublicTrApprovalsSettingsDTO restoreValues(@PathVariable("organizationId") UUID organizationId,
                                               @PathVariable("settingId") UUID settingId);
}
