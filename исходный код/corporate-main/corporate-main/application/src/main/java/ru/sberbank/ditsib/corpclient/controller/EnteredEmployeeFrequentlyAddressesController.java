package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller of entered employee addresses.
 */
@Deprecated
@RequestMapping({"/self/addresses/frequently", "/self/addresses/frequently/"})
@Tag(name = "Частые адреса вошедшего сотрудника", description = "Набор операций для работы с частыми адресами " +
        "вошедшего сотрудника")
public interface EnteredEmployeeFrequentlyAddressesController {

    /**
     * Get address.
     *
     * @param request input request.
     * @return collection of addresses.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех частых адресов вошедшего пользователя")
    ResponseEntity<byte[]> getAll(HttpServletRequest request);

    /**
     * Nulling address.
     *
     * @param request   request input request.
     */
    @DeleteMapping(value = {"{addressId}", "{addressId}/"})
    @Operation(summary = "Обнуление использования", description = "Обнуление счетчика использования адреса")
    ResponseEntity<byte[]> nullingUsage(@PathVariable("addressId") UUID addressId, HttpServletRequest request);

}
