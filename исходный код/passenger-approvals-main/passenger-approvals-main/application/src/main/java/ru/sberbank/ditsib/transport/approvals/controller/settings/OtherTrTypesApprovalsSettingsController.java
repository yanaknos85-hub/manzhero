package ru.sberbank.ditsib.transport.approvals.controller.settings;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.approvals.dto.settings.NewOtherTrTypesApprovalsSettingsDTO;
import ru.sberbank.ditsib.transport.approvals.dto.settings.OtherTrTypesApprovalsSettingsDTO;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * Контроллер для работы с настройками согласований заявок на поездки на всех видах транспорта, кроме такси и
 * общественного
 **/
@RequestMapping({"{organizationId}/settings/request/other", "{organizationId}/settings/request/other/"})
@Tag(name = "Настройки согласований заявок на всех видах транспорта, кроме такси и общественного",
     description = "Модуль управления согласованиями. Виды транспорта: личный / каршеринг / самокат / велосипед / " +
                   "скутер")
public interface OtherTrTypesApprovalsSettingsController {
    
    /**
     * Добавить настройку согласования заявки на поездку на транспорте, кроме общественного и такси
     * @return новая настройка согласования заявки на поездку на транспорте, кроме общественного и такси
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление",
               description = "Добавление настройки согласований заявок на поездку на транспорте, кроме общественного и такси")
    OtherTrTypesApprovalsSettingsDTO add(
            @PathVariable("organizationId") UUID organizationId,
            @RequestBody @Valid NewOtherTrTypesApprovalsSettingsDTO newRequest);

    /**
     * Получить настройки согласования заявки на поездку на транспорте, кроме общественного и такси по идентификатору
     * @return настройки согласования заявки на поездку на транспорте, кроме общественного и такси по идентификатору
     */
    @GetMapping(path = "/{transportType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получить настройки согласований заявок на поездку на транспорте, кроме общественного и " +
                         "такси по идентификатору",
               description = "Получить настройки согласований заявок на поездку на транспорте, кроме общественного и " +
                             "такси по идентификатору")
    OtherTrTypesApprovalsSettingsDTO get(@PathVariable("organizationId") UUID organizationId,
                                         @PathVariable("transportType") String transportType);
    
    /**
     * Обновить настройки согласования заявки на поездку на транспорте, кроме общественного и такси
     */
    @PutMapping(path = "/{transportType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Обновить настройки согласований заявок на поездку на транспорте, кроме общественного и такси",
               description = "Обновить настройки согласований заявок на поездку на транспорте, кроме общественного и такси")
    void update(@PathVariable("organizationId") UUID organizationId,
                @PathVariable("transportType") String transportType,
                @RequestBody @Valid NewOtherTrTypesApprovalsSettingsDTO newRequest);
    
    /**
     * Удалить настройку согласования заявки на поездку на транспорте, кроме общественного и такси
     */
    @DeleteMapping("/{transportType}")
    @ResponseBody
    @Operation(summary = "Удалить настройку согласования заявки на поездку на транспорте, кроме общественного и такси",
               description = "Удалить настройку согласования заявки на поездку на транспорте, кроме общественного и такси")
    void delete(@PathVariable("organizationId") UUID organizationId,
                @PathVariable("transportType") String transportType);
    
    /**
     * Сбросить настройку согласования заявки на значение по умолчанию
     * @return сброшенная настройка согласования заявки
     */
    @PostMapping("/{transportType}/restore")
    @ResponseBody
    @Operation(summary = "Сбросить настройку согласования заявки на значение по умолчанию",
               description = "Сбросить настройку согласования заявки на значение по умолчанию")
    OtherTrTypesApprovalsSettingsDTO restoreValues(@PathVariable("organizationId") UUID organizationId,
                                                   @PathVariable("transportType") String transportType);
}
