package ru.sber.transport.tariff_fleet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.tariff_fleet.dto.*;
import ru.sber.transport.tariff_fleet.dto.service_point.UploadServicePointsDto;

import java.util.UUID;

/**
 * Контроллер по договорам
 */
@RequestMapping("contracts")
@Tag(name = "Договор", description = "Контроллер для работы с договорами")
public interface ContractController {
    
    /**
     * Добавление договора
     *
     * @param abstractContractPostDto {@link AbstractContractPostDto}
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление", description = "Добавление нового договора")
    void create(
            @Valid @RequestBody AbstractContractPostDto abstractContractPostDto
               );
    
    /**
     * Добавление договора (все организации)
     *
     * @param abstractContractPostAllOrganizationsDto {@link AbstractContractPostAllOrganizationsDto}
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "all-organizations", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление (все организации)", description = "Добавление нового договора (все организации)")
    void createAllOrganizations(
            @Valid @RequestBody AbstractContractPostAllOrganizationsDto abstractContractPostAllOrganizationsDto
                               );
    
    /**
     * Добавление договора (своя организации)
     *
     * @param abstractContractPostSelfOrganizationDto {@link AbstractContractPostSelfOrganizationDto}
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "self-organization", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление (своя организации)", description = "Добавление нового договора (своя организация)")
    void createSelfOrganization(
            @Valid @RequestBody AbstractContractPostSelfOrganizationDto abstractContractPostSelfOrganizationDto,
            @Parameter(hidden = true) Authentication authentication
                               );
    
    /**
     * Получение списка договоров
     *
     * @param abstractSearchContractDto {@link AbstractSearchContractDto}
     *
     * @return {@link Page<AbstractContractGetDto>}
     */
    @PostMapping(value = "search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение списка", description = "Получение списка договоров с постраничной выгрузкой")
    @SuppressWarnings("java:S1452")
    Page<? extends AbstractContractGetDto> search(
            @RequestBody @Valid AbstractSearchContractDto abstractSearchContractDto
                                                 );
    
    /**
     * Получение списка договоров (все организации)
     *
     * @param abstractSearchContractAllOrganizationsDto {@link AbstractSearchContractAllOrganizationsDto}
     *
     * @return {@link Page<AbstractContractGetAllOrganizationsDto>}
     */
    @PostMapping(value = "search/all-organizations", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение списка (все организации)", description = "Получение списка договоров с постраничной выгрузкой (все организации)")
    @SuppressWarnings("java:S1452")
    Page<? extends AbstractContractGetAllOrganizationsDto> searchAllOrganizations(
            @RequestBody @Valid AbstractSearchContractAllOrganizationsDto abstractSearchContractAllOrganizationsDto
                                                                                 );
    
    /**
     * Получение списка договоров (своя организация)
     *
     * @param abstractSearchContractSelfOrganizationDto {@link AbstractSearchContractSelfOrganizationDto}
     *
     * @return {@link Page<AbstractContractGetSelfOrganizationDto>}
     */
    @PostMapping(value = "search/self-organization", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение списка (своя организация)",
               description = "Получение списка договоров с постраничной выгрузкой (своя организация)")
    @SuppressWarnings("java:S1452")
    Page<? extends AbstractContractGetSelfOrganizationDto> searchSelfOrganization(
            @RequestBody @Valid AbstractSearchContractSelfOrganizationDto abstractSearchContractSelfOrganizationDto,
            @Parameter(hidden = true) Authentication authentication
                                                                                 );
    
    /**
     * Редактирование договора
     *
     * @param contractId Идентификатор записи о договоре
     * @param abstractContractPatchDto {@link AbstractContractPatchDto}
     */
    @PatchMapping(value = "{contractId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Редактирование", description = "Редактирование договора")
    void edit(
            @NotNull @PathVariable UUID contractId,
            @Valid @RequestBody AbstractContractPatchDto abstractContractPatchDto
             );
    
    /**
     * Получение договора по идентификатору
     *
     * @param contractId Идентификатор записи о договоре
     *
     * @return {@link AbstractGetContractByIdDto}
     */
    @GetMapping(value = "{contractId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение", description = "Получение договора по идентификатору")
    AbstractGetContractByIdDto get(@NotNull @PathVariable UUID contractId);

    /**
     * @param contractId Идентификатор записи о договоре
     *
     * @deprecated Деактивация договора по идентификатору
     */
    @PatchMapping(value = "{contractId}/deactivate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Деактивация", description = "Деактивация договора по идентификатору")
    void deactivate(@NotNull @PathVariable UUID contractId);
    
    
    @GetMapping(value = "{contractId}/self-organization", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение договора своя организация", description = "Получение договора по ID (с точками обслуживания) - своя организация")
    ContractWithServicePointsFileDto getContractSelfWithFuelStationPointsFile(
            @NotNull @PathVariable UUID contractId, @Parameter(hidden = true) Authentication authentication
                                                                             );

    @GetMapping(value = "{contractId}/all-organizations", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение договора все организации", description = "Получение договора по ID (с точками обслуживания) - все организации")
    ContractWithServicePointsFileDto getContractAllWithFuelStationPointsFile(@NotNull @PathVariable UUID contractId);

    @PatchMapping(value = "{contractId}/service-points/all-organizations",
                  consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                  produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Валидация файла сервисных точек",
               description = "Валидация файла с точками обслуживания для редактирования (все организации)")
    UploadServicePointsDto validateServicePointsFileAllOrganizations(
            @NotNull @PathVariable UUID contractId,
            @RequestPart("documentType") String documentType,
            @RequestPart("file") MultipartFile file
                                                                    );

    @PatchMapping(value = "{contractId}/service-points/self-organization",
                  consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                  produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Валидация файла сервисных точек",
               description = "Валидация файла с точками обслуживания для редактирования (своя организация)")
    UploadServicePointsDto validateServicePointsFileSelfOrganization(
            @NotNull @PathVariable UUID contractId,
            @RequestPart("documentType") String documentType,
            @RequestPart("file") MultipartFile file,
            @Parameter(hidden = true) Authentication authentication
                                                                    );

    @PatchMapping(value = "{contractId}/self-organization", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Обновление договора своя организация", description = "Обновление договора своя организация")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updatePartiallyContractSelfOrganization(
            @NotNull @PathVariable UUID contractId,
            @Valid @RequestBody AbstractContractUpdateRequest request,
            @Parameter(hidden = true) Authentication authentication
                                                );

    @PatchMapping(value = "{contractId}/all-organizations", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Обновление договора все организации", description = "Обновление договора все организации")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updatePartiallyContractAllOrganizations(@NotNull @PathVariable UUID contractId,  @Valid @RequestBody AbstractContractUpdateRequest request);

    @PatchMapping(value = "{contractId}/deactivate/self-organization", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Деактивация договора своя организация", description = "Деактивация договора своя организация")
    void deactivateContractSelfOrganization(@NotNull @PathVariable UUID contractId, @Parameter(hidden = true) Authentication authentication);

    @PatchMapping(value = "{contractId}/deactivate/all-organizations", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Деактивация договора все организации", description = "Деактивация договора все организации")
    void deactivateContractAllOrganizations(@NotNull @PathVariable UUID contractId);

}
