package ru.sberbank.ditsib.corpclient.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.corpclient.dto.NewPersonalCarDTO;
import ru.sberbank.ditsib.corpclient.dto.PersonalCarDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.UUID;


/**
 * Controller for working with personal cars.
 */
@RequestMapping({"/{organizationId}/departments/{departmentId}/employees/{employeeId}/cars", "/{organizationId}/departments/{departmentId}/employees/{employeeId}/cars/"})
@Tag(name = "Личные автомобили", description = "Набор операций для работы с личными автомобилями сотрудников")
public interface PersonalCarController {
    
    /**
     * @param organizationId organization Id
     * @param departmentId department id
     * @param employeeId employee id
     *
     * @return request mapping string for specified id
     */
    static String getApiMappingByIds(
            @NotNull UUID organizationId,
            @NotNull UUID departmentId,
            @NotNull UUID employeeId
                                    ) {
        return "/{organizationId}/departments/{departmentId}/employees/{employeeId}/cars/"
                       .replace("{employeeId}", employeeId.toString())
                       .replace("{departmentId}", departmentId.toString())
                       .replace("{organizationId}", organizationId.toString()) + "/";
    }
    
    
    /**
     * Add a new personal car to existing employee
     *
     * @param organizationId organization Id
     * @param departmentId department id
     * @param employeeId employee id
     * @param newPersonalAuto new personal car data.
     *
     * @return added personal car.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление", description = "Добавление нового личного авто")
    PersonalCarDTO savePersonalCar(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody NewPersonalCarDTO newPersonalAuto,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                  );
    
    /**
     * Edit personal car.
     *
     * @param organizationId organization Id
     * @param departmentId department id
     * @param employeeId employee id
     * @param newData new data of personal car.
     */
    @PutMapping(value = {"{autoId}", "{autoId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces =
            MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных личного автомобиля")
    void editPersonalAuto(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("autoId") UUID autoId,
            @Valid @RequestBody PersonalCarDTO newData,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                         );
    
    /**
     * Delete personal car.
     *
     * @param organizationId organization Id
     * @param departmentId department id
     * @param employeeId employee id
     * @param autoId ID of personal car to delete.
     */
    @DeleteMapping(value = {"{autoId}", "{autoId}/"})
    @Operation(summary = "Удаление", description = "Удаление данных личного автомобиля")
    void deletePersonalAuto(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("autoId") UUID autoId
                           );
    
    /**
     * Get personal car with ID.
     *
     * @param organizationId organization Id
     * @param departmentId department id
     * @param employeeId employee id
     * @param autoId ID of personal car to get.
     *
     * @return personal car.
     */
    @GetMapping(value = {"{autoId}", "{autoId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных личного автомобиля")
    PersonalCarDTO getPersonalCar(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("autoId") UUID autoId
                                 );
    
    /**
     * Get all personal cars.
     *
     * @param organizationId organization Id
     * @param departmentId department id
     * @param employeeId employee id
     *
     * @return list of personal cars.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех личных автомобилей сотрудника")
    Collection<PersonalCarDTO> getPersonalCars(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @PathVariable("employeeId") UUID employeeId
                                                        );
    
}
