package ru.sber.transport.tariff_fleet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.dto.ContractorDto;

import java.util.List;

/**
 * Контроллер по контрагентам
 */
@RequestMapping("contractors")
@Tag(name = "Контрагент", description = "Контроллер для работы с контрагентами")
public interface ContractorController {
    
    
    /**
     * Получение списка всех активных контрагентов
     *
     * @return список контрагентов {@link ContractorDto}
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение", description = "Получение списка всех активных контрагентов")
    List<ContractorDto> getAllActive();
    
    /**
     * Получение списка контрагентов у которых есть договоры - все организации по типу документа
     * @param documentType
     * @return список контрагентов {@link ContractorDto}
     */
    @GetMapping(value = "/{documentType}/all-organizations", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение списка контрагентов (все организации)",
               description = "Получение списка контрагентов у которых есть договоры - все организации")
    List<ContractorDto> getAllOrganization(@Parameter(description = "Тип документа", required = true) @PathVariable("documentType") DocumentType documentType);
    
    /**
     * Получение списка контрагентов у которых есть договоры - своя организация по типу документа
     * @param documentType
     * @return список контрагентов {@link ContractorDto}
     */
    @GetMapping(value = "/{documentType}/self-organization", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение списка контрагентов (своя организация)",
               description = "Получение списка контрагентов у которых есть договоры - своя организация")
    List<ContractorDto> getSelfOrganization(
            @Parameter(description = "Тип документа", required = true) @PathVariable("documentType") DocumentType documentType,
            @Parameter(hidden = true) Authentication authentication);
    


}
