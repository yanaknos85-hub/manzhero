package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.sberbank.ditsib.corpclient.dto.*;
import ru.sberbank.ditsib.corpclient.dto.docs.EmployeeDocumentFileDTO;

import java.io.IOException;
import java.util.UUID;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

@RequestMapping({"/{organizationId}/departments/{departmentId}/employees",
        "/{organizationId}/departments/{departmentId}/employees/"
        })
@Tag(name = "Сотрудники", description = "Набор операций для работы с сотрудниками")
public interface EmployeeController {
    
    /**
     * @param organizationId organization id
     * @param departmentId department id
     *
     * @return request mapping string for specified id
     */
    static String getApiMappingByOrgIdAndDepartmentId(@NotNull UUID organizationId, @NotNull UUID departmentId) {
        return "/{organizationId}/departments/{departmentId}/employees"
                .replace("{organizationId}", organizationId.toString())
                .replace("{departmentId}", departmentId.toString())+"/";
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление", description = "Добавление нового сотрудника",
               security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
    EmployeeDTO saveEmployee(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @Valid @RequestBody NewEmployeeDTO newEmployee
                            );
    
    /**
     * Add a new employee
     *
     * @param organizationId organization Id
     * @param departmentId departmentId.
     */
    @GetMapping(value = {"isUserExist/{userId}", "isUserExist/{userId}/"})
    @Operation(summary = "Опрос", description = "Опрос наличия userId",
               security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
    void isUserExist(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @PathVariable("userId") UUID userId
                            );
    
    
    /**
     * Edit employee.
     *
     * @param organizationId organization id
     * @param departmentId department id
     * @param newData new data of employee.
     */
    @PutMapping(value = {"{employeeId}", "{employeeId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных сотрудника",
               security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
    void editEmployee(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody NewEmployeeDTO newData
                     );
    
    /**
     * Delete employee.
     *
     * @param organizationId organization id
     * @param departmentId department id
     * @param employeeId ID of employee to delete.
     */
    @DeleteMapping(value = {"{employeeId}", "{employeeId}/"})
    @Operation(summary = "Удаление", description = "Удаление данных сотрудника",
               security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
    void deleteEmployee(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @PathVariable("employeeId") UUID employeeId
                       );
    
    /**
     * Get employee with ID.
     *
     * @param organizationId organization id
     * @param departmentId department id
     * @param employeeId ID of employee to get.
     *
     * @return employee.
     */
    @GetMapping(value = {"{employeeId}", "{employeeId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных сотрудника",
               security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
    EmployeeDTO get(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @PathVariable("employeeId") UUID employeeId
                           );
    
    /**
     * Get employee with user ID.
     *
     * @param organizationId organizationid
     * @param userId optional id of user
     *
     * @return employee.
     */
    @GetMapping(value = {"user/{userId}", "user/{userId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение сотрудника по пользователю", description = "Получение сотрудника по пользователю")
    EmployeeDTO getEmployeeByUser(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("userId") UUID userId
                                 );
    
    /**
     * Get all employees of department.
     *
     * @param organizationId organization id
     * @param departmentId department id
     *
     * @return list of employees.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех сотрудников",
               security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
    Iterable<HasEmployeeData> getEmployees(
            @Parameter(description = "Идентификатор организации", required = true)
            @PathVariable("organizationId") UUID organizationId,
            @Parameter(description = "Идентификатор подразделения", required = true)
            @PathVariable("departmentId") UUID departmentId,
            @Parameter(description = "Параметры запроса") EmployeeParameters parameters,
            @RequestParam(value = "projection", defaultValue = "FULL") EmployeeProjection projection
                                                  );
    
    
    /**
     * Получить список сотрудников по должности
     *
     * @param organizationId id организации
     * @param positionId id должности
     *
     * @return список сотрудников
     */
    @GetMapping(value = {"position/{positionId}", "position/{positionId}/"},produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение сотрудников по должности", description = "Получение данных сотрудников, имеющих " +
                                                                             "указанную должность",
               security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
    Iterable<EmployeeDTO> getEmployeesByPosition(
            @Parameter(description = "Идентификатор организации", required = true)
            @PathVariable("organizationId") UUID organizationId,
            @Parameter(description = "Идентификатор должности", required = true)
            @PathVariable("positionId") UUID positionId,
            @Parameter(description = "Параметры запроса") EmployeeParameters parameters
                                                  );
    
    /**
     * Get all employees.
     *
     * @param organizationId organization id
     * @param departmentId department id
     * @param searchString search string
     *
     * @return list of employees.
     */
    @GetMapping(value = {"search", "search/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск сотрудника", description = "Поиск сотрудника по его данным",
               security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
    Iterable<EmployeeDTO> searchEmployees(
            @Parameter(description = "Идентификатор организации", required = true)
            @PathVariable("organizationId") UUID organizationId,
            @Parameter(description = "Идентификатор подразделения", required = true)
            @PathVariable("departmentId") UUID departmentId,
            @Parameter(description = "Строка поиска", required = true)
            @RequestParam("fio") @NotBlank String searchString,
            @Parameter(description = "Параметры запроса") EmployeeParameters parameters
            );

    /**
     * @param organizationId    id корпоративного клиента
     * @param departmentId      id подразделения (пока не используется)
     * @param employeeId        id сотрудника (используется как префикс в полном имени объекта в S3-хранилище)
     * @param file              Содержимое файла, которое нужно загрузить
     * @return                  Атрибуты загруженного файла
     */
    @PostMapping(
            value = {"{employeeId}/files/upload", "{employeeId}/files/upload/"},
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseBody
    @Operation(summary = "Загрузка файла на сервер", description = "Загрузка файла на сервер")
    ResponseEntity<EmployeeDocumentFileDTO> uploadFile(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @PathVariable("employeeId") UUID employeeId,
            @RequestParam(value = "file") MultipartFile file,
            @RequestHeader(HttpHeaders.CONTENT_LENGTH) long length
    ) throws IOException, InterruptedException;

    /**
     * @param organizationId    id корпоративного клиента
     * @param departmentId      id подразделения (пока не используется)
     * @param employeeId        id сотрудника (используется как префикс в полном имени объекта в S3-хранилище)
     * @param fileName          Имя файла, который нужно выгрузить с сервера
     * @return                  Содержимое файла
     */
    @GetMapping("{employeeId}/files/download/{fileName}")
    @Operation(summary = "Выгрузка файла с сервера", description = "Выгрузка файла с сервера")
    @ResponseBody
    ResponseEntity<byte[]> downloadFile(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable String fileName
    );
}
