package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.corpclient.dto.NewAddressDto;

import java.util.UUID;

/**
 * Controller of entered employee addresses.
 */
@Deprecated
@RequestMapping({"/self/addresses/favorite", "/self/addresses/favorite/"})
@Tag(name = "Любимые адреса вошедшего сотрудника", description = "Набор операций для работы с любимыми адресами " +
        "вошедшего сотрудника")
public interface EnteredEmployeeFavoriteAddressesController {

    /**
     * Add a new address.
     *
     * @param newData new data of address.
     * @param request input request.
     * @return added address.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление", description = "Добавление нового любимого адреса")
    @ResponseBody
    ResponseEntity<byte[]> addAddress(HttpServletRequest request, @RequestBody NewAddressDto newData);

    /**
     * Edit a new address.
     *
     * @param newData new data of address.
     * @param request input request.
     */
    @PutMapping(value = {"{id}", "{id}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных любимого адреса")
    ResponseEntity<byte[]> editAddress(@PathVariable("id") UUID id, HttpServletRequest request, @RequestBody NewAddressDto newData);

    /**
     * Delete address.
     *
     * @param request input request.
     */
    @DeleteMapping(value = {"{id}", "{id}/"})
    @Operation(summary = "Удаление", description = "Удаление данных любимого адреса")
    ResponseEntity<byte[]> deleteAddress(@PathVariable("id") UUID id, HttpServletRequest request);

    /**
     * Get address.
     *
     * @param request input request.
     * @return address with ID.
     */
    @GetMapping(value = {"{id}", "{id}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение", description = "Получение данных любимого адреса")
    @ResponseBody
    ResponseEntity<byte[]> get(@PathVariable("id") UUID id, HttpServletRequest request);

    /**
     * Get address.
     *
     * @param request input request.
     * @return collection of addresses.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение всех", description = "Получение данных всех любимых адресов")
    @ResponseBody
    ResponseEntity<byte[]> getAll(HttpServletRequest request);
}
