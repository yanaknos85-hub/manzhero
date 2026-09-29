package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.corpclient.dto.NewMeetingAddressesDTO;

import java.util.UUID;

/**
 * Controller of meeting addresses.
 */
@Deprecated
@RequestMapping({"/self/addresses/meeting", "/self/addresses/meeting/"})
@Tag(name = "Общие адреса для сотрудников", description = "Набор операций для работы с общими адресами сотрудников")
public interface EnteredEmployeeMeetingAddressesController {
    /**
     * Add a new address.
     *
     * @param newData new data of address.
     * @param request request input request.
     * @return added address.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление", description = "Добавление нового общего адреса")
    @ResponseBody
    ResponseEntity<byte[]> addAddress(HttpServletRequest request, @Valid @RequestBody NewMeetingAddressesDTO newData);

    /**
     * Edit a new address.
     *
     * @param newData new data of address.
     * @param request request input request.
     */
    @PutMapping(value = {"{id}", "{id}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных общего адреса")
    ResponseEntity<byte[]> editAddress(@PathVariable("id") UUID id, HttpServletRequest request, @Valid @RequestBody NewMeetingAddressesDTO newData);

    /**
     * Delete address.
     *
     * @param request request input request.
     */
    @DeleteMapping(value = {"{id}", "{id}/"})
    @Operation(summary = "Удаление", description = "Удаление данных общего адреса")
    ResponseEntity<byte[]> deleteAddress(@PathVariable("id") UUID id, HttpServletRequest request);

    /**
     * Get all address data.
     *
     * @param request request input request.
     * @return collection of addresses.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение всех", description = "Получение данных всех общих адресов")
    @ResponseBody
    ResponseEntity<byte[]> getAll(HttpServletRequest request, JwtAuthenticationToken token);

}
