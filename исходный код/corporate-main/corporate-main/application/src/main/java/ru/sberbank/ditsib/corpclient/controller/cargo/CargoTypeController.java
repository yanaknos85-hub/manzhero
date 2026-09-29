package ru.sberbank.ditsib.corpclient.controller.cargo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.corpclient.dto.cargo.CargoTypeCategoryDto;
import ru.sberbank.ditsib.corpclient.dto.cargo.CargoTypeDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Controller for working with cargo types.
 */
@Tag(name = "Грузы. Справочник видов грузов", description = "Набор операций для работы со справочников видов грузов")
public interface CargoTypeController {

    String CARGO_TYPE_BASE_URL = "/cargo/type";
    String CARGO_TYPE_WITH_ORGANIZATION_BASE_URL = "/{organizationId}" + CARGO_TYPE_BASE_URL;

    /**
     * Get type by ID.
     *
     * @param typeId ID of type to get.
     * @return type.
     */
    @GetMapping(value = CARGO_TYPE_WITH_ORGANIZATION_BASE_URL + "/{typeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение", description = "Получение вида груза")
    CargoTypeDto getType(@PathVariable UUID organizationId, @PathVariable("typeId") @NotNull UUID typeId,
                         @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Add a new type.
     *
     * @param newData new data of type.
     * @return type.
     */
    @PostMapping(value = CARGO_TYPE_WITH_ORGANIZATION_BASE_URL, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление", description = "Добавление вида груза")
    CargoTypeDto addType(@PathVariable UUID organizationId,
                         @Valid @RequestBody CargoTypeDto newData,
                         @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Add a new type without organization.
     *
     * @param newData new data of type.
     * @return type.
     */
    @PostMapping(value = CARGO_TYPE_BASE_URL, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление", description = "Добавление вида груза")
    CargoTypeDto addType(@Valid @RequestBody CargoTypeDto newData,
                         @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Edit existing type.
     *
     * @param typeId  ID package to edit.
     * @param newData new data of package.
     * @return type
     */
    @PostMapping(value = CARGO_TYPE_WITH_ORGANIZATION_BASE_URL + "/{typeId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение вида груза")
    CargoTypeDto updateType(@PathVariable UUID organizationId, @PathVariable("typeId") UUID typeId,
                            @Valid @RequestBody CargoTypeDto newData,
                            @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

    /**
     * Get all types.
     *
     * @return list of types.
     */
    @GetMapping(value = CARGO_TYPE_WITH_ORGANIZATION_BASE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение всех", description = "Получение всех активных видов грузов")
    Collection<CargoTypeDto> getTypes(@PathVariable UUID organizationId,
                                      @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Delete type.
     *
     * @param typeId ID type to delete.
     */
    @DeleteMapping(value = CARGO_TYPE_WITH_ORGANIZATION_BASE_URL + "/{typeId}")
    @Operation(summary = "Удаление", description = "Удаление вид груза")
    void deleteType(@PathVariable UUID organizationId, @PathVariable("typeId") UUID typeId,
                    @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Запрос списка видов груза
     *
     * @return cargo transport type list.
     */
    @GetMapping(value = CARGO_TYPE_BASE_URL + "/types", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех видов груза", description = "Получение всех видов груза")
    List<CargoTypeCategoryDto> getAllTypes();

    /**
     * Запрос списка категорий груза
     *
     * @return cargo categories.
     */
    @GetMapping(value = CARGO_TYPE_BASE_URL + "/categories", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех категорий груза", description = "Получение всех категорий груза")
    List<CargoTypeCategoryDto> getAllCategory();

    /**
     * Поиск видов груза
     *
     * @param searchText text to searching.
     * @return list of cargo types.
     */
    @GetMapping(value = CARGO_TYPE_WITH_ORGANIZATION_BASE_URL + "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск видов груза", description = "Поиск видов груза")
    List<CargoTypeDto> search(@PathVariable UUID organizationId,
                              @RequestParam("text") @NotBlank String searchText,
                              @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Поиск видов груза с пустой организаией
     *
     * @param searchText text to searching.
     * @return list of cargo types.
     */
    @GetMapping(value = CARGO_TYPE_BASE_URL + "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск видов груза", description = "Поиск видов груза")
    List<CargoTypeDto> search(@RequestParam("text") @NotBlank String searchText,
                              @Parameter(hidden = true) JwtAuthenticationToken authentication);
}
