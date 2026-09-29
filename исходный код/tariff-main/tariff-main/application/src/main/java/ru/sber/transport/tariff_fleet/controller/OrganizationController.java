package ru.sber.transport.tariff_fleet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sber.transport.tariff_fleet.dto.GetAllActiveOrganizationNamesDto;
import ru.sber.transport.tariff_fleet.dto.GetDepartmentsInfo;
import ru.sber.transport.tariff_fleet.dto.OrganizationsDepartmentsSearchDto;

import java.util.List;

/**
 * Контроллер по организациям
 */
@RequestMapping("organization")
@Tag(name = "Организация", description = "Контроллер для работы с организациями")
public interface OrganizationController {
    
    
    /**
     * Получение списка названий всех активных организаций
     *
     * @return {@link List<GetAllActiveOrganizationNamesDto>}
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение", description = "Получение списка названий всех активных организаций")
    List<GetAllActiveOrganizationNamesDto> getAllActiveNames();
    
    /**
     * Получение списка подразделений организации
     *
     * @return {@link List<GetDepartmentsInfo>}
     */
    @PostMapping(value = "/department", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение", description = "Получение списка подразделений")
    List<GetDepartmentsInfo> getOrganizationsDepartments(@Valid @RequestBody OrganizationsDepartmentsSearchDto dto);
}
