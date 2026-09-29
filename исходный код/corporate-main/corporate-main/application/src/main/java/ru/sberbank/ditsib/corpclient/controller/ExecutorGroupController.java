package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.corpclient.dto.ExecutorGroupDTO;
import ru.sberbank.ditsib.corpclient.dto.ExecutorGroupParameters;
import ru.sberbank.ditsib.corpclient.dto.NewExecutorGroupDTO;

import java.util.List;
import java.util.UUID;

import static ru.sberbank.ditsib.corpclient.controller.ExecutorGroupController.API_MAPPING;

/**
 * Controller for working with executor groups.
 */
@RequestMapping(API_MAPPING)
@Tag(name = "Группы исполнителей", description = "Набор операций для работы с группами исполнителей")
public interface ExecutorGroupController {

    String API_MAPPING = "/executorGroup";

    /**
     * Add a new executor group.
     *
     * @param newExecutorGroup new executor group data.
     * @return added executor group.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление", description = "Добавление новой группы исполнителей")
    ExecutorGroupDTO saveExecutorGroup(@Valid @RequestBody NewExecutorGroupDTO newExecutorGroup);

    /**
     * Get executor group with id.
     *
     * @param executorGroupId ID of executor group to get.
     * @return executor group.
     */
    @GetMapping(value = {"{executorGroupId}", "{executorGroupId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение", description = "Получение данных о группе исполнителей")
    ExecutorGroupDTO getExecutorGroup(@PathVariable UUID executorGroupId,
                                      @Parameter(hidden = true) JwtAuthenticationToken authentication);


    /**
     * Delete executor group with id.
     *
     * @param executorGroupId ID of executor group to delete.
     */
    @DeleteMapping(value = "{executorGroupId}")
    @Operation(summary = "Удаление", description = "Удаление группы исполнителей")
    void deleteExecutorGroup(@PathVariable UUID executorGroupId);


    /**
     * Edit executor group.
     *
     * @param executorGroupId executor group Id
     * @param newData         new data of executor group.
     */
    @PutMapping(value = {"{executorGroupId}", "{executorGroupId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных о группе исполнителей")
    void editExecutorGroup(@PathVariable("executorGroupId") UUID executorGroupId,
                           @Valid @RequestBody NewExecutorGroupDTO newData);

    /**
     * Get all executor groups.
     *
     * @return list of executor groups.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение всех групп исполнителей", description = "Получение данных всех групп исполнителей")
    Iterable<ExecutorGroupDTO> getExecutorGroups(
            @Parameter(description = "Параметры запроса") ExecutorGroupParameters parameters
    );

    /**
     * Получение группы исполнителей по id пользователя
     *
     * @param employeeId id пользователя
     * @return DTO с данными группы исполнителей
     */
    @GetMapping(value = {"/affiliation/{employeeId}", "/affiliation/{employeeId}/"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    ExecutorGroupDTO getExecutorGroupByEmployeeId(@PathVariable UUID employeeId,
                                                  @RequestParam(required = false) List<UUID> geoZoneIds);

}
