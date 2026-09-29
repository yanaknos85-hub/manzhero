package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.corpclient.dto.TransportOrgDto;
import ru.sberbank.ditsib.corpclient.dto.constant.TransportTypeEnumDTO;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RequestMapping(value = {"/transportorg", "/transportorg/"})
@Tag(name = "Активация типов транспорта", description = "Активация типов транспорта для организаций")
public interface TransportOrgController {

    /**
     * Получение всех типов настроек совместных поездок.
     * Настройки общие для любой организации, ее ID не проверяется
     *
     * @return все типы настроек
     */
    @GetMapping(value = {"/org/{organizationId}", "/org/{organizationId}/"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение активных типов транспорта для организации",
            description = "Получение активных типов транспорта для организации")
    List<TransportTypeEnumDTO> get(@PathVariable UUID organizationId);

    /**
     * Получение всех типов настроек совместных поездок.
     * Настройки общие для любой организации, ее ID не проверяется
     *
     * @return все типы настроек
     */
    @GetMapping(value = {"/{transportServiceTypeId}/org/{organizationId}", "/{transportServiceTypeId}/org/{organizationId}/"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение активных типов транспорта для организации с фильтрацией по типу сервиса",
            description = "Получение активных типов транспорта для организации с фильтрацией по типу сервиса")
    List<TransportTypeEnumDTO> getByServiceType(@PathVariable String transportServiceTypeId,
                                                @PathVariable UUID organizationId);

    /**
     * Получение всех типов транспорта.
     * Настройки общие для любой организации, ее ID не проверяется
     *
     * @return все типы настроек
     */
    @GetMapping(value = {"all/org/{organizationId}", "all/org/{organizationId}/"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех типов транспорта для организации",
            description = "Получение всех типов транспорта для организации")
    List<TransportOrgDto> getAll(@PathVariable UUID organizationId);

    /**
     * Получение всех типов настроек совместных поездок.
     * Настройки общие для любой организации, ее ID не проверяется
     *
     * @return все типы настроек
     */
    @GetMapping(value = {"all/{transportServiceTypeId}/org/{organizationId}", "all/{transportServiceTypeId}/org/{organizationId}/"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех типов транспорта для организации с фильтрацией по типу сервиса",
            description = "Получение всех типов транспорта для организации с фильтрацией по типу сервиса")
    List<TransportOrgDto> getAllByServiceType(@PathVariable String transportServiceTypeId,
                                              @PathVariable UUID organizationId);

    /**
     * Получение всех типов настроек совместных поездок.
     * Настройки общие для любой организации, ее ID не проверяется
     *
     * @return все типы настроек
     */
    @PostMapping(value = {"/org/{organizationId}/tt/{transportType}", "/org/{organizationId}/tt/{transportType}/"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление типа транспорта", description = "Добавление типа транспорта")
    List<String> add(@PathVariable UUID organizationId,
                     @PathVariable String transportType);

    /**
     * Получение всех типов настроек совместных поездок.
     * Настройки общие для любой организации, ее ID не проверяется
     */
    @PostMapping(value = {"/batch/org/{organizationId}", "/batch/org/{organizationId}/"},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Активация/деактивация типов транспорта",
            description = "Активация/деактивация типов транспорта")
    void addBatch(@PathVariable UUID organizationId,
                  @Valid @RequestBody List<TransportOrgDto> list);

    /**
     * Получение всех типов настроек совместных поездок.
     * Настройки общие для любой организации, ее ID не проверяется
     *
     * @return все типы настроек
     */
    @DeleteMapping(value = {"/org/{organizationId}/tt/{transportType}", "/org/{organizationId}/tt/{transportType}/"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Удаление типа транспорта", description = "Удаление типа транспорта")
    List<String> delete(@PathVariable UUID organizationId,
                        @PathVariable String transportType);
}
