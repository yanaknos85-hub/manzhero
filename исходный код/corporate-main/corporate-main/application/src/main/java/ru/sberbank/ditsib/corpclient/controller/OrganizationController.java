package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.sberbank.ditsib.corpclient.dto.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Controller for working with organizations.
 */
@RequestMapping({OrganizationController.API_MAPPING, OrganizationController.API_MAPPING + "/"})
@Tag(name = "Организации", description = "Набор операций для работы с организациями")
public interface OrganizationController {
    String API_MAPPING = "/";
    String ORGANIZATION_EMPLOYEE_API_MAPPING = "{organizationId}/employees";
    
    /**
     * @param orgId organizationid
     *
     * @return request mapping string for specified id
     */
    static String getEmployeeApiMappingByOrgId(@NotNull UUID orgId) {
        return "/" + ORGANIZATION_EMPLOYEE_API_MAPPING.replace("{organizationId}", orgId.toString() + "/");
    }
    
    /**
     * Edit organization.
     *
     * @param newData new data of organization.
     */
    @PutMapping(value = {"{organizationId}", "{organizationId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных организаций")
    void editOrganization(
            @PathVariable("organizationId") UUID organizationId,
            @Valid @RequestBody NewOrganizationDTO newData
                         );
    
    /**
     * Delete organization.
     *
     * @param organizationId ID of organization to delete.
     */
    @DeleteMapping(value = {"{organizationId}", "{organizationId}/"})
    @Operation(summary = "Удаление", description = "Удаление данных организации")
    void deleteOrganization(@PathVariable("organizationId") UUID organizationId);
    
    /**
     * Get organization with ID.
     *
     * @param organizationId ID of organization to get.
     *
     * @return organization.
     */
    @GetMapping(value = {"{organizationId}", "{organizationId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных организации")
    ResponseEntity<OrganizationDTO> getOrganization(@PathVariable("organizationId") UUID organizationId);
    
    /**
     * Get all organizations.
     *
     * @return list of organizations.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех организаций")
    ResponseEntity<Iterable<OrganizationSelectDTO>> getOrganizations(
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @Parameter(description = "Параметры запроса") OrganizationParameters parameters,
            @RequestParam(name = "projection", defaultValue = "FULL") OrganizationProjection projection
                                                              );
    
    /**
     * Get all employees of organization.
     *
     * @param organizationId organization id
     *
     * @return list of employees.
     */
    @GetMapping(value = {ORGANIZATION_EMPLOYEE_API_MAPPING, ORGANIZATION_EMPLOYEE_API_MAPPING + "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех сотрудников", description = "Получение данных всех сотрудников организации")
    Iterable<HasEmployeeData> getEmployees(
            @Parameter(description = "Идентификатор организации", required = true)
            @PathVariable("organizationId") UUID organizationId,
            @Parameter(description = "Параметры запроса") EmployeeParameters parameters,
            @RequestParam(value = "projection", defaultValue = "FULL") EmployeeProjection projection
                                                      );
    
    /**
     * Get all employees.
     *
     * @param organizationId organizationId
     * @param searchString search string
     *
     * @return list of employees.
     *
     * @deprecated Запрос не соответсвует REST, лучше использовать {@link #getEmployees(UUID, EmployeeParameters, EmployeeProjection)}
     */
    @GetMapping(value = {API_MAPPING + ORGANIZATION_EMPLOYEE_API_MAPPING + "search", API_MAPPING + ORGANIZATION_EMPLOYEE_API_MAPPING + "search/"},
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск", description = "Поиск сотрудников", deprecated = true)
    @Deprecated(forRemoval = true)
    Iterable<EmployeeDTO> searchEmployees(
            @PathVariable("organizationId") UUID organizationId,
            @RequestParam("fio") @NotBlank String searchString,
            @Parameter(description = "Параметры запроса") EmployeeParameters parameters
                                         );
    
    /**
     * Search employees.
     *
     * @param searchString search string
     *
     * @return list of employees.
     */
    @GetMapping(value = {"employees/search", "employees/search/"},
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск", description = "Поиск сотрудников с учетом типа оргструктуры и организации")
    Iterable<EmployeeDTO> searchAllEmployees(
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @RequestParam("fio") @NotBlank String searchString,
            @Parameter(description = "Параметры запроса") EmployeeParameters parameters
                                         );

    /**
     * Get all employees.
     *
     * @param organizations list organization id for search
     * @param departments list department id for search
     *
     * @return list of employees.
     */
    @GetMapping(value = {"employees/search_eg", "employees/search_eg/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск сотрудников для групп исполнителей",
            description = "Поиск сотрудников по их принадлежности организации(ям) и/или департаменту(ам)")
    Iterable<CustomerDTO> searchEmployeesByOrgsAndDeps(
            @NotEmpty @RequestParam("organizations") List<UUID> organizations,
            @RequestParam(value = "departments", required = false) List<UUID> departments,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
    );
    
    @PostMapping(value = {"/logo/{organizationId}", "/logo/{organizationId}/"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Выгрузка логотипа", description = "Выгрузка логотипа организации")
    ResponseEntity<Void> uploadFile(
            @Parameter(description = "Файл логотипа") @RequestParam("logo") MultipartFile file,
            @Parameter(description = "Идентификатор организации")
            @PathVariable("organizationId") UUID organizationId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @RequestHeader(HttpHeaders.CONTENT_LENGTH) long length
                   ) throws IOException;
    
    @GetMapping(value = {"/logo/{organizationId}", "/logo/{organizationId}/"})
    @Operation(summary = "Загрузка логотипа", description = "Загрузка логотипа организации")
    ResponseEntity<byte[]> downloadFile(
            @Parameter(description = "Идентификатор организации")
            @PathVariable("organizationId") UUID organizationId
                                       );
    
    @PutMapping(value = {"/resend-all", "/resend-all/"})
    void resendAll(@RequestParam("key") String key);
    
}
