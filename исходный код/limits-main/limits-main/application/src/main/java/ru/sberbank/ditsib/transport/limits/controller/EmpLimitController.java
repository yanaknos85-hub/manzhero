package ru.sberbank.ditsib.transport.limits.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.limits.dto.EmpLimitSharingDTO;
import ru.sberbank.ditsib.transport.limits.model.GetLimitDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Controller interface for employee limits.
 */
@RequestMapping(value = "/emplimits")
@Tag(name = "Личные лимиты сотрудников", description = "Набор операций для работы с личными лимитами")
public interface EmpLimitController {

    /**
     * Get limit with ID.
     *
     * @param limitId ID of limit to get.
     * @return limit.
     * @deprecated use `GET /{limitId}` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = "{limitId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных личного лимита")
    GetLimitDTO get(@PathVariable("limitId") @NotNull UUID limitId);

    /**
     * Get all limits.
     *
     * @return list of limits.
     * @deprecated use `GET /?limitType=EMPLOYEE` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение всех личных лимитов")
    Collection<GetLimitDTO> getAll();

    /**
     * Get limit by employee.
     *
     * @param employeeId employee id.
     * @return list of limit dto.
     * @deprecated use `GET /?limitType=EMPLOYEE&amp;employeeId={employeeId}` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = "/getByEmployee/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение лимитов сотрудника", description = "Получение лимитов сотрудника")
    List<GetLimitDTO> getByEmployee(@PathVariable("employeeId") @NotNull UUID employeeId);

    /**
     * Get limit by employee and year.
     *
     * @param employeeId employee id.
     * @return limit dto.
     * @deprecated use `GET /?limitType=EMPLOYEE&amp;employeeId={employeeId}&amp;year={year}` instead
     */
    @GetMapping(value = "/getByEmployeeAndYear/{employeeId}/year/{year}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Deprecated(forRemoval = true)
    @Operation(summary = "Получение лимита по сотруднику и году", description = "Получение лимита по сотруднику и году")
    GetLimitDTO getByEmployeeAndYear(
            @PathVariable("employeeId") @NotNull UUID employeeId,
            @PathVariable("year") @NotNull Integer year
    );

    /**
     * Get limit by employee and year.
     *
     * @param employeeId employee id.
     * @return limit dto.
     * @deprecated use `GET /?limitType=EMPLOYEE&amp;employeeId={employeeId}&amp;year={year}` instead
     */
    @GetMapping(value = "/getByEmployeeAndYearFull/{employeeId}/year/{year}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Deprecated(forRemoval = true)
    @Operation(summary = "Получение лимита по сотруднику и году", description = "Получение лимита по сотруднику и году")
    GetLimitDTO getByEmployeeAndYearFull(
            @PathVariable("employeeId") @NotNull UUID employeeId,
            @PathVariable("year") @NotNull Integer year
    );

    /**
     * Make employee limit.
     */
    @PostMapping(value = "/make_emp_limits", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Создание новых личных лимитов с распределением",
            description = "Создание новых личных лимитов с распределением")
    void makeEmployeeLimits(
            @Valid @RequestBody List<EmpLimitSharingDTO> limitSharingDTOList,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Make employee limit.
     */
    @PostMapping(value = "/make_emp_limit", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Создание нового личного лимита с распределением",
            description = "Создание нового личного лимита с распределением")
    void makeEmployeeLimit(
            @Valid @RequestBody EmpLimitSharingDTO limitSharingDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Change employee limit.
     *
     * @deprecated Вывести из эксплуатации!
     */
    @PutMapping(value = "/change_emp_limit", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Увеличение или уменьшение личного лимита с распределением",
            description = "Увеличение или уменьшение  личного лимита с распределением")
    @Deprecated(forRemoval = true)
    // not used on front?
    void changeEmployeeLimit(
            @Valid @RequestBody EmpLimitSharingDTO limitSharingDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Delete employee limit.
     */
    @PutMapping(value = "/close_emp_limit/{empLimitId}")
    @Operation(summary = "Удаление личного лимита с распределением",
            description = "Удаление личного лимита с распределением")
    void closeEmployeeLimit(
            @PathVariable("empLimitId") @NotNull UUID empLimitId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);
}
