package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller of entered employee addresses.
 */
@Deprecated
@RequestMapping({"/self/addresses", "/self/addresses/"})
@Tag(name = "Адреса вошедшего сотрудника", description = "Набор операций для работы с адресами вошедшего " +
        "сотрудника")
public interface EnteredEmployeeAddressesController {

    /**
     * Get address.
     *
     * @param request input request.
     * @return collection of addresses.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех адресов")
    ResponseEntity<byte[]> getAll(HttpServletRequest request);

}
