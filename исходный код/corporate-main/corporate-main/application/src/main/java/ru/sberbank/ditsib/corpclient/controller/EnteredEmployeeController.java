package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.corpclient.dto.EmployeePatchField;
import ru.sberbank.ditsib.corpclient.dto.PatchData;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.util.List;
import java.util.Set;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

/**
 * Controller of entered employee.
 */
@RequestMapping({"/self", "/self/"})
@Tag(name = "Сотрудник", description = "Набор операций для работы с вошедшим сотрудником")
public interface EnteredEmployeeController {

    @PatchMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Частичное изменение", description = "Частичное изменение данных сотрудника",
            security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
    void updateEmployee(
            @RequestBody List<PatchData<EmployeePatchField>> data,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

    @PatchMapping(value = "/consent/")
    @Operation(summary = "Подписание Пдн", description = "Подписание Пдн сотрудником")
    void signPdn(@Parameter(hidden = true) JwtAuthenticationToken authentication);
    
    /**
     * @return список разрешенных по должности классов такси
     */
    @GetMapping(value = {"allowed-classes", "allowed-classes/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение разрешенных классов такси", description = "Получение разрешенных по должности " +
                                                                              "классов такси")
    Set<TaxiClass> getAllowedTaxiClasses();
    
    /**
     * Resend employees.
     */
    @PutMapping(value = {"/resend", "/resend/"})
    @Operation(summary = "Обновление данных всех сотрудников в других сервисах",
               description = "Обновление данных всех сотрудников в других сервисах",
               security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
    void resendEmployees();
}
