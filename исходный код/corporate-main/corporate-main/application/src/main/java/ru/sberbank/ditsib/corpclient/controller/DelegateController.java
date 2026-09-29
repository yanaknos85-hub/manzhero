package ru.sberbank.ditsib.corpclient.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.corpclient.dto.*;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Controller for working with delegates.
 */
@RequestMapping({"/{organizationId}/departments/{departmentId}/delegates", "/{organizationId}/departments/{departmentId}/delegates/"})
@Tag(name = "Делегаты", description = "Набор операций для работы с делегатами")
public interface DelegateController {
    
    /**
     * @param organizationId organization Id
     * @param departmentId department id
     *
     * @return request mapping string for specified id
     */
    static String getApiMappingByIds(
            @NotNull UUID organizationId,
            @NotNull UUID departmentId
                                    ) {
        return "/{organizationId}/departments/{departmentId}/delegates/"
                       .replace("{departmentId}", departmentId.toString())
                       .replace("{organizationId}", organizationId.toString()) + "/";
    }
    
    /**
     * Add a new delegate.
     *
     * @param organizationId organization Id
     * @param departmentId department id
     * @param delegateRecordDTO new data of delegate.
     *
     * @return added delegate.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление", description = "Добавление нового делегата")
    GetDelegateRecordDTO add(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @RequestBody @Valid DelegateRecordDTO delegateRecordDTO
                            );
    
    /**
     * Edit specified delegate.
     *
     * @param organizationId organization Id
     * @param departmentId department id
     * @param recordId ID of delegate record
     * @param delegate new data of delegate
     */
    @PutMapping(value = {"{delegateId}", "{delegateId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных делегата (Изменяемы только дата начала и дата " +
                                                    "завершения)")
    void edit(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @PathVariable("delegateId") UUID recordId,
            @RequestBody @Valid DelegateRecordDTO delegate
             );
    
    /**
     * Delete delegate with ID.
     *
     * @param organizationId organization Id
     * @param departmentId department id
     * @param recordId ID of delegate to delete.
     */
    @DeleteMapping({"{delegateId}", "{delegateId}/"})
    @Operation(summary = "Удаление", description = "Удаление данных делегата")
    void delete(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @PathVariable("delegateId") UUID recordId
               );
    
    /**
     * Get delegate by ID.
     *
     * @param organizationId organization Id
     * @param departmentId department id
     * @param recordId id of delegate record to get data.
     *
     * @return delegate.
     */
    @GetMapping(value = {"{delegateId}", "{delegateId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных делегата")
    GetDelegateRecordDTO get(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @PathVariable("delegateId") UUID recordId
                            );
    
    /**
     * Get all delegates.
     *
     * @param organizationId organization Id
     * @param departmentId department id
     * @param supervisorId supervisor id
     * @param page index of page.
     * @param size size of page.
     *
     * @return get collection of delegate records..
     */
    @GetMapping(value = {"supervisors/{supervisorId}", "supervisors/{supervisorId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение списка делегатов пользователя", description = "Получение данных всех делегатов " +
            "указанного " +
            "руководителя")
    Page<GetDelegateRecordDTO> getAll(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @PathVariable("supervisorId") UUID supervisorId,
            @RequestParam("date") Optional<String> date,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size
    );

    /**
     * Get all delegates for specified transport type
     *
     * @param organizationId organization Id
     * @param departmentId department id
     * @param supervisorId supervisor id
     * @param transportType transport type
     *
     * @return get collection of delegate candidates.
     */
    @GetMapping(value = {"candidates/{supervisorId}/{transportType}", "candidates/{supervisorId}/{transportType}/"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение списка кандидатов в делегаты",
               description = "Получение данных всех делегатов указанного руководителя по указанному типу транспорта" +
                             " на указанную дату")
    Iterable<EmployeeDTO> getAllCandidates(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @PathVariable("supervisorId") UUID supervisorId,
            @PathVariable("transportType") TransportTypeEnum transportType,
            @NotNull @RequestParam("date") String date,
            @Parameter(description = "Параметры запроса") EmployeeParameters parameters
                                                    );
    
    
}
