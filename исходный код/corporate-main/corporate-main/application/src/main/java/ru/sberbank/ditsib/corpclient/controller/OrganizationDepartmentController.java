package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.corpclient.dto.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Controller for working with departments in one organization.
 */
@RequestMapping({"/{organizationId}/departments", "/{organizationId}/departments/"})
@Tag(name = "Подразделения", description = "Набор операций для работы с подразделениями")
public interface OrganizationDepartmentController {
    
    /**
     * @param orgId organization id
     *
     * @return request mapping string for specified id
     */
    static String getApiMappingByOrgId(@NotNull UUID orgId) {
        return "/{organizationId}/departments/".replace("{organizationId}", orgId.toString()) + "/";
    }
    
    /**
     * Add a new department to existing organization
     *
     * @param organizationId organization Id
     * @param newDepartment new department data.
     *
     * @return added department.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление", description = "Добавление нового подразделения")
    DepartmentDTO saveDepartment(
            @PathVariable("organizationId") UUID organizationId,
            @Valid @RequestBody NewDepartmentDTO newDepartment
                                );

    /**
     * Edit department.
     *
     * @param organizationId organization Id
     * @param newData new data of department.
     */
    @PutMapping(value = {"{departmentId}", "{departmentId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных подразделения")
    void editDepartment(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @Valid @RequestBody NewDepartmentDTO newData
                       );
    
    /**
     * Delete department.
     *
     * @param organizationId organization Id
     * @param departmentId ID of department to delete.
     */
    @DeleteMapping(value = {"{departmentId}", "{departmentId}/"})
    @Operation(summary = "Удаление", description = "Удаление данных подразделения")
    void deleteDepartment(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId
                         );
    
    /**
     * Get department with ID.
     *
     * @param organizationId organization Id
     * @param departmentId ID of department to get.
     *
     * @return department.
     */
    @GetMapping(value = {"{departmentId}", "{departmentId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных подразделения")
    DepartmentDTO getDepartment(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId
                               );
    
    /**
     * Получить дочерние подразделения подразделения.
     *
     * @param organizationId Id организации
     * @param departmentId Id подразделения.
     *
     * @return набор подразделений.
     */
    @GetMapping(value = {"{departmentId}/children", "{departmentId}/children/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение дочерних подразделений", description = "Получение дочерних подразделений")
    Iterable<DepartmentDTO> getChildrenDepartments(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @Parameter(description = "Параметры запроса") DepartmentParameters parameters
                                                       );
    
    
    /**
     * Get department with ID.
     *
     * @param organizationId organization Id
     * @param departmentSet Set of department id to get.
     *
     * @return department.
     */
    @PostMapping(value = {"departments_search", "departments_search/"}, consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных нескольких подразделений")
    Iterable<DepartmentSelectDTO> getDepartment(
            @PathVariable("organizationId") UUID organizationId,
            @RequestBody Set<UUID> departmentSet,
            @Parameter(description = "Параметры запроса") DepartmentParameters parameters
                                                         );
    
    /**
     * Get all departments.
     *
     * @param organizationId organization Id
     *
     * @return list of departments.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение активных подразделений", description = "Получение данных всех подразделений(без " +
                                                                           "удалённых)")
    Iterable<DepartmentSelectDTO> getDepartments(
            @PathVariable("organizationId") UUID organizationId,
            @Parameter(description = "Параметры запроса") DepartmentParameters parameters,
            @RequestParam(name = "projection", defaultValue = "FULL") DepartmentProjection projection
                                                          );
    
    //
    /**
     * Get all departments.
     *
     * @param organizationId organization Id
     *
     * @return list of departments.
     */
    @GetMapping(value = {"all", "all/"},produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех подразделений(с удалёнными)")
    Iterable<DepartmentDTO> getAllDepartments(
            @PathVariable("organizationId") UUID organizationId,
            @Parameter(description = "Параметры запроса") DepartmentParameters parameters
                                             );


    @GetMapping(value = {"level", "level/"},produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение подразделений", description = "Получение подразделений 1, 2 и 3 уровня")
    Iterable<DepartmentDTO> getLevelDepartments (@PathVariable("organizationId") UUID organizationId);

    /**
     * Check filial flags.
     *
     * @param organizationId organization Id
     *
     * @return data for flags.
     */
    @GetMapping(value = {"checkFilialFlagAll", "checkFilialFlagAll/"},produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Проверка флагов филиала", description = "Проверка флагов филиала")
    CheckFilialFlagResutlDTO checkFilialFlagAll(@PathVariable("organizationId") UUID organizationId);

    /**
     * Check filial flags for department.
     *
     * @param organizationId organization Id
     * @param departmentId department Id
     *
     * @return true if check ok, false otherwise.
     */
    @GetMapping(value = {"checkFilialFlag/{departmentId}", "checkFilialFlag/{departmentId}/"})
    @Operation(summary = "Проверка флагов филиала для подразделения",
               description = "Проверка флагов филиала для подразделения")
    boolean checkFilialFlag(@PathVariable("organizationId") UUID organizationId,
                            @PathVariable("departmentId") UUID departmentId);

    /**
     * Get upper level departments.
     *
     * @param organizationId organization Id
     *
     * @return list of departments.
     */
    @GetMapping(value = {"getUpperLevelActiveDepartments", "getUpperLevelActiveDepartments/"},produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение верхнеуровневых подразделений", description = "Получение верхнеуровневых подразделений")
    Iterable<DepartmentDTO> getUpperLevelActiveDepartments(@PathVariable("organizationId") UUID organizationId);

    /**
     * Редактирование флага автоназначение водителя.
     *
     * @param organizationId organizationId.
     * @param departmentId departmentId.
     * @param list values.
     */
    @PatchMapping(value = {"{departmentId}", "{departmentId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Частичное изменение", description = "Частичное изменение")
    void editPartial(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @RequestBody List<PatchData<DepartmentPatchField>> list
    );
}
