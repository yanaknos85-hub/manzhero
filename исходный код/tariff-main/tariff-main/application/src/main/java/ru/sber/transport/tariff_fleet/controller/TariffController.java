package ru.sber.transport.tariff_fleet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.tariff_fleet.dto.*;

import java.util.UUID;

/**
 * Контроллер по тарифам
 */
@RequestMapping("tariffs")
@Tag(name = "Тариф", description = "Контроллер для работы с тарифами")
public interface TariffController {
    
    /**
     * Добавление нового тарифа
     *
     * @param abstractTariffPostDto {@link AbstractTariffPostDto}
     * @param authentication {@link Authentication}
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление", description = "Добавление нового тарифа")
    void create(
            @Valid @RequestBody AbstractTariffPostDto abstractTariffPostDto,
            @Parameter(hidden = true) Authentication authentication
               );
    
    /**
     * Получение списка тарифов
     *
     * @param abstractSearchTariffDto {@link AbstractSearchTariffDto}
     *
     * @return {@link Page<AbstractTariffGetDto>}
     */
    @PostMapping(value = "search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение списка", description = "Получение списка тарифов с постраничной выгрузкой")
    @SuppressWarnings("java:S1452")
    Page<? extends AbstractTariffGetDto> search(
            @RequestBody @Valid AbstractSearchTariffDto abstractSearchTariffDto
                                               );
    
    /**
     * Получение тарифа по идентификатору
     *
     * @param id Идентификатор записи о тарифе
     *
     * @return {@link AbstractTariffGetByIdDto}
     */
    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение тарифа по идентификатору", description = "Получение тарифа по идентификатору")
    AbstractTariffGetByIdDto getById(@NotNull @PathVariable UUID id);
    
    /**
     * Редактирование тарифа
     *
     * @param id Идентификатор записи о тарифе
     * @param abstractTariffPatchDto {@link AbstractTariffPatchDto}
     */
    @PatchMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Редактирование тарифа", description = "Редактирование тарифа у договора")
    void edit(
            @NotNull @PathVariable UUID id,
            @Valid @RequestBody AbstractTariffPatchDto abstractTariffPatchDto
             );
    
    /**
     * Деактивация тарифа по идентификатору
     *
     * @param id Идентификатор записи о тарифе
     */
    @PatchMapping(value = "{id}/deactivate")
    @Operation(summary = "Деактивация", description = "Деактивация тарифа по идентификатору")
    void deactivate(@NotNull @PathVariable UUID id);

    @PostMapping(value = "self-organization", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Создание тарифа своя организация", description = "Создание тарифа своя организация")
    @ResponseStatus(HttpStatus.CREATED)
    void createTariffSelfOrganization(@Valid @RequestBody AbstractCreateTariffRequest request, @Parameter(hidden = true) Authentication authentication);

    @PostMapping(value = "all-organizations", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Создание тарифа все организации", description = "Создание тарифа все организации")
    @ResponseStatus(HttpStatus.CREATED)
    void createTariffAllOrganizations(@Valid @RequestBody AbstractCreateTariffRequest request);

    @GetMapping(value = "{id}/self-organization", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение тарифа по идентификатору своя организация", description = "Получение тарифа по идентификатору своя организация")
    AbstractTariffResponse getTariffSelfOrganization(@NotNull @PathVariable UUID id, @Parameter(hidden = true) Authentication authentication);

    @GetMapping(value = "{id}/all-organizations", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение тарифа по идентификатору все организации", description = "Получение тарифа по идентификатору все организации")
    AbstractTariffResponse getTariffAllOrganizations(@NotNull @PathVariable UUID id);

    @PatchMapping(value = "{id}/deactivate/self-organization", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Деактивация тарифа по идентификатору своя организация", description = "Деактивация тарифа по идентификатору своя организация")
    void deactivateTariffSelfOrganization(@NotNull @PathVariable UUID id, @Parameter(hidden = true) Authentication authentication);

    @PatchMapping(value = "{id}/deactivate/all-organizations", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Деактивация тарифа по идентификатору все организации", description = "Деактивация тарифа по идентификатору все организации")
    void deactivateTariffAllOrganizations(@NotNull @PathVariable UUID id);

    @PostMapping(value = "search/self-organization", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение списка тарифов по параметрам - своя организация", description = "Получение списка тарифов по параметрам - своя организация")
    Page<SearchTariffResponse>  searchTariffSelfOrganization(@Valid @RequestBody SearchTariffRequest request, @Parameter(hidden = true) Authentication authentication);

    @PostMapping(value = "search/all-organizations", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение списка тарифов по параметрам - все организации", description = "Получение списка тарифов по параметрам - все организации")
    Page<SearchTariffResponse> searchTariffAllOrganizations(@Valid @RequestBody SearchTariffRequest request);
}
