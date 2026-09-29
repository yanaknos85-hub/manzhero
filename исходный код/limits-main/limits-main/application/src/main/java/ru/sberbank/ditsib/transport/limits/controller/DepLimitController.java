package ru.sberbank.ditsib.transport.limits.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.dto.*;
import ru.sberbank.ditsib.transport.limits.model.GetLimitDTO;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Controller interface for employee limits.
 */
@RequestMapping(value = "/deplimits")
@Tag(name = "Лимиты подразделений", description = "Набор операций для работы с лимитами подразделений")
public interface DepLimitController {
    
    /**
     * Add a new limit.
     *
     * @param depLimitPrimaryDTO new limit data.
     * @param organizationId ID of organization.
     * @param authentication authentication data.
     *
     * @return added limit.
     * @deprecated use `POST /organization/{organizationId}/department` instead
     */
    @Deprecated(since = "2023-07-31")
    @PostMapping(value = "/add/{organizationId}",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    GetLimitDTO add(@PathVariable("organizationId") UUID organizationId,
                              @Valid @RequestBody DepLimitPrimaryDTO depLimitPrimaryDTO,
                              @Parameter(hidden = true) JwtAuthenticationToken authentication);
    
    /**
     * Edit limit.
     *
     * @param limitId limit id.
     * @param depLimitPrimaryDTO new data of limit.
     * @param authentication authentication data.
     * @param source request source.
     * @deprecated use `PUT /{limitId}` instead
     */
    @Deprecated(since = "2023-07-31")
    @PutMapping(value = "{limitId}",
                consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение данных лимита подразделения", description = "Изменение данных лимита подразделения")
    void edit(
            @PathVariable("limitId") UUID limitId,
            @Valid @RequestBody DepLimitPrimaryDTO depLimitPrimaryDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @RequestHeader(value = "x-source", required = false) String source
             );
    
    /**
     * Edit limit.
     *
     * @param limitId limit id.
     * @param depLimitEditDTO new data of limit.
     * @param authentication authentication data.
     * @param source request source.
     * @deprecated use `PATCH /{limitId}` instead
     */
    @Deprecated(since = "2023-07-31")
    @PutMapping(value = "/editStatusOrFlags/{limitId}",
                consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение статуса или флагов лимита подразделения",
               description = "Изменение статуса или флагов лимита подразделения")
    void editStatusOrFlags(
            @PathVariable("limitId") UUID limitId,
            @Valid @RequestBody DepLimitEditDTO depLimitEditDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @RequestHeader(value = "x-source", required = false) String source
             ) throws JsonProcessingException;
    
    /**
     * Edit use this limit flag.
     *
     * @param limitId limit id.
     * @param useThisLimit flag value.
     * @param authentication authentication data.
     * @param source request source.
     * @deprecated use `PATCH /{limitId}` instead
     */
    @Deprecated(since = "2023-07-31")
    @PutMapping(value = "{limitId}/{useThisLimit}")
    @Operation(summary = "Изменение флага <Использовать лимит моего подразделения>",
               description = "Изменение флага <Использовать лимит моего подразделения>")
    void editUseThisLimitFlag(
            @PathVariable("limitId") UUID limitId,
            @PathVariable("useThisLimit") Boolean useThisLimit,
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @RequestHeader(value = "x-source", required = false) String source
             );
    
    /**
     * Get limit with ID.
     *
     * @param limitId ID of limit to get.
     *
     * @return request.
     * @deprecated use `GET /{limitId}` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = "{limitId}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных лимита подразделения")
    GetLimitDTO get(@PathVariable("limitId") @NotNull UUID limitId);
    
    /**
     * Get all limits.
     *
     * @return list of limits.
     * @deprecated use `GET /?limitType=DEPARTMENT` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех лимитов подразделений", description = "Получение всех лимитов подразделений")
    Collection<GetLimitDTO> getAll();
    
    /**
     * Get limit by department.
     *
     * @param departmentId department id.
     *
     * @return list of limit dto.
     * @deprecated use `GET /?limitType=DEPARTMENT&amp;departmentId={departmentId}` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = "/getByDepartment/{departmentId}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение лимитов подразделения",
               description = "Получение лимитов подразделения")
    List<GetLimitDTO> getByDepartment(@PathVariable("departmentId") @NotNull UUID departmentId);
    
    /**
     * Get limit by department and year.
     *
     * @param departmentId department id.
     * @param year year.
     *
     * @return limit dto.
     * @deprecated use `GET /?limitType=DEPARTMENT&amp;departmentId={departmentId}&amp;year={year}` instead
     */
    @GetMapping(value = "/getByDepartmentAndYear/{departmentId}/year/{year}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Deprecated(forRemoval = true)
    @Operation(summary = "Получение лимита по подразделению и году",
               description = "Получение лимита по подразделению и году")
    GetLimitDTO getByDepartmentAndYear(@PathVariable("departmentId") @NotNull UUID departmentId,
                                       @PathVariable("year") @NotNull Integer year);
    
    /**
     * Get limit by department and year.
     *
     * @param departmentId department id.
     * @param year year.
     *
     * @return limit dto.
     * @deprecated use `GET /?limitType=DEPARTMENT&amp;departmentId={departmentId}&amp;year={year}` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = "/getByDepartmentAndYearAll/{departmentId}/year/{year}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение лимитов по подразделению и году",
               description = "Получение лимитов по подразделению и году")
    List<GetLimitDTO> getByDepartmentAndYearAll(@PathVariable("departmentId") @NotNull UUID departmentId,
                                                @PathVariable("year") @NotNull Integer year);
    
    /**
     * Get limit by department and year and service type.
     *
     * @param departmentId department id
     * @param year year
     * @param limitServiceType service type
     *
     * @return limit dto.
     * @deprecated use `GET /?limitType=DEPARTMENT&amp;departmentId={departmentId}&amp;year={year}&amp;limitServiceType={limitServiceType}` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = "/getByDepartmentAndYearAndLimitServiceType/{departmentId}/year/{year}/limitServiceType/{limitServiceType}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение лимита по подразделению и году и типу услуги",
               description = "Получение лимита по подразделению и году и типу услуги")
    GetLimitDTO getByDepartmentAndYearAndLimitServiceType(
            @PathVariable("departmentId") @NotNull UUID departmentId,
            @PathVariable("year") @NotNull Integer year,
            @PathVariable("limitServiceType") @NotNull String limitServiceType);
    
    /**
     * Get limit by department and year and service type.
     *
     * @param departmentId department id.
     * @param year year.
     * @param limitServiceType service type
     *
     * @return limit dto.
     * @deprecated use `GET /?limitType=DEPARTMENT&amp;departmentId={departmentId}&amp;year={year}&amp;limitServiceType={limitServiceType}` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = "/getByDepartmentAndYearAndLimitServiceTypeFull/{departmentId}/year/{year}/limitServiceType/{limitServiceType}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение полных данных лимита по подразделению и году и типу услуги",
               description = "Получение полных данных лимита по подразделению и году и типу услуги")
    GetLimitDTO getByDepartmentAndYearAndLimitServiceTypeFull(@PathVariable("departmentId") @NotNull UUID departmentId,
                                           @PathVariable("year") @NotNull Integer year,
                                           @PathVariable("limitServiceType") @NotNull String limitServiceType);
    
    /**
     * Share limit for many departments.
     *
     * @param parentLimitId ID of parent limit.
     * @param dtoList new sharing data.
     * @param authentication authentication.
     *
     */
    @PostMapping(value = "/share_primary/{parentLimitId}",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Первичное распределение", description = "Распределение лимита между подразделениями")
    void shareLimitPrimary(
            @PathVariable("parentLimitId") UUID parentLimitId,
            @Valid @RequestBody List<PrimarySharingDTO> dtoList,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                          );
    
    /**
     * Share limit for many departments.
     *
     * @param dtoList new sharing data. (REDIRECTED)
     * @param parentLimitId ID of parent limit.
     * @param authentication authentication.
     * @param source request source.
     * @return list of IDs.
     */
    @PostMapping(value = "/share_secondary/{parentLimitId}",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Вторичное распределение", description = "Распределение лимита между подразделениями")
    List<UUID> shareLimitSecondary(
            @PathVariable("parentLimitId") UUID parentLimitId,
            @Valid @RequestBody List<DepLimitSharingDTO> dtoList,
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @RequestHeader(value = "x-source", required = false) String source
                                  );
    
    /**
     * Share limit for many departments.
     *
     * @param dtoList new sharing data.
     * @param parentLimitId ID of parent limit.
     * @param authentication authentication.
     *
     */
    @PostMapping(value = "/share_economy/{parentLimitId}",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Распределение из экономии", description = "Распределение из экономии")
    void shareLimitEconomy(
            @PathVariable("parentLimitId") UUID parentLimitId,
            @Valid @RequestBody List<DepLimitEconomyDTO> dtoList,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                          );
    
    /**
     * Reshare limits by transport types.
     *
     * @param limitReSharingByTransportTypeDTO new limit data.
     * @param authentication authentication data.
     * @deprecated use `PUT /transport-type/reshare` instead
     */
    @Deprecated(since = "2023-07-31")
    @PostMapping(value = "/reshare_transporttypes", consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Перераспределение между видами транспорта",
               description = "Перераспределение между видами транспорта")
    void reShareLimitBetweenTransportTypes(
            @Valid @RequestBody LimitReSharingByTransportTypeDTO limitReSharingByTransportTypeDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                          );
    
    
    /**
     * Reshare limits by departments.
     *
     * @param limitReSharingByDepartmentDTO new limit data.
     * @param authentication authentication data.
     * @param source request source.
     * @deprecated use `PUT /departments/reshare` instead
     */
    @Deprecated(since = "2023-07-31")
    @PostMapping(value = "/reshare_departments", consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Перераспределение между подразделениями",
               description = "Перераспределение между подразделениями")
    void reShareLimitDepartments(
            @Valid @RequestBody LimitReSharingByDepartmentDTO limitReSharingByDepartmentDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @RequestHeader(value = "x-source", required = false) String source
                                       );
    
    /**
     * Reshare limits from child department to parent.
     *
     * @param limitResharingDTO new limit data.
     * @param authentication authentication data.
     * @deprecated use `PUT /reshare` instead
     */
    @Deprecated(since = "2023-07-31")
    @PostMapping(value = "/reshare_transfer", consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Перераспределение между лимитами",
               description = "Перераспределение между лимитами")
    void reShareLimit(
            @Valid @RequestBody LimitResharingDTO limitResharingDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                     );
    
    /**
     * Get siblings for department.
     *
     * @param percent percent.
     * @param year year.
     * @param sum sum.
     * @param departmentId ID of department.
     * @param transportType type of transport.
     * @return list of departments.
     */
    @GetMapping(value = "/siblings/{departmentId}/{percent}/{transportType}/{year}/{sum}",
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение смежников подразделения", description = "Получение смежников подразделения")
    List<Department> getSiblings(@PathVariable("departmentId") UUID departmentId,
                                 @PathVariable("percent") Integer percent,
                                 @PathVariable("transportType") TransportTypeEnum transportType,
                                 @PathVariable("year") Integer year,
                                 @PathVariable("sum") Long sum);
}
