package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.corpclient.dto.purpose.NewTripPurposeDTO;
import ru.sberbank.ditsib.corpclient.dto.purpose.TripPurposeDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.UUID;

/**
 * Controller for working with trip purposes.
 */
@RequestMapping({"/{organizationId}/purposes", "/{organizationId}/purposes/"})
@Tag(name = "Цели", description = "Набор операций для работы с целями поездки")
public interface TripPurposeController {
    
    /**
     * Get trip purpose with ID.
     *
     * @param uuid ID of purpose to get.
     * @param organizationId organization of trip purpose.
     *
     * @return purpose.
     */
    @GetMapping(value = {"{purposeId}", "{purposeId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение", description = "Получение цели поездки")
    TripPurposeDTO getPurpose(
            @PathVariable("purposeId") @NotNull UUID uuid,
            @PathVariable("organizationId") UUID organizationId);

    /**
     * Add a new purpose.
     *
     * @param organizationId organization of trip purpose.
     * @param newData new data of trip purpose.
     *
     * @return trip purpose.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление", description = "Добавление цели поездки")
    @ResponseBody
    TripPurposeDTO savePurpose(
            @PathVariable("organizationId") UUID organizationId,
            @Valid @RequestBody NewTripPurposeDTO newData
    );

    /**
     * Edit a new purpose.
     * @param organizationId organization of trip purpose.
     * @param purposeId ID purpose to edit.
     * @param newData new data of purpose.
     *
     */
    @PutMapping(value = {"{id}", "{id}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение цели поездки")
    void editPurpose(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("id") UUID purposeId,
            @Valid @RequestBody NewTripPurposeDTO newData
    );

    /**
     * Get all purposes.
     * @param organizationId organization of trip purpose.
     *
     * @return list of purposes.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение всех", description = "Получение всех активных целей поездки организации")
    Collection<TripPurposeDTO> getPurposes(@PathVariable("organizationId") UUID organizationId);

    /**
     * Get all trip purposes
     *
     * @return list of trip purposes.
     */
    @GetMapping(value = {"/search", "/search/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Поиск", description = "Поиск цели поездки")
    Collection<TripPurposeDTO> getTripPurposesBySearchString(@RequestParam("value") @NotBlank String value,
                                                             @PathVariable("organizationId") UUID organizationId);

    /**
     * Get all trip purposes
     *
     * @return list of trip purposes.
     */
    @GetMapping(value = {"/all", "/all/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение всех", description = "Получение всех целей поездки организации")
    Collection<TripPurposeDTO> getAllPurposes(@PathVariable("organizationId") UUID organizationId);

    /**
     * Get employee trip purposes
     *
     * @param authentication параметры аутентификации
     *
     * @return list of employee trip purposes.
     */
    @GetMapping(value = {"/search_by_employee", "/search_by_employee/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Поиск", description = "Поиск цели поездки у сотрудника по его признакам")
    Collection<TripPurposeDTO> getTripPurposesByEmployee(JwtAuthenticationToken authentication, @PathVariable("organizationId") UUID organizationId);

    /**
     * Delete trip purposes.
     *
     * @param organizationId organization of trip purpose.
     * @param purposeId ID trip purposes to delete.
     */
    @DeleteMapping(value = {"{id}", "{id}/"})
    @Operation(summary = "Удаление", description = "Удаление цели поездки")
    void deletePurpose(@PathVariable("organizationId") UUID organizationId,
                       @PathVariable("id") UUID purposeId);
}
