package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.corpclient.dto.*;

import java.util.Set;
import java.util.UUID;

/**
 * Controller for working with departments.
 */
@RequestMapping({"/departments"})
@Tag(name = "Подразделения", description = "Набор операций для работы с подразделениями")
public interface DepartmentController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех подразделений по фильтру", description = "Получение данных всех подразделений по фильтру")
    Iterable<DepartmentSelectDTO> searchDepartments(
            @RequestParam(required = false) Set<UUID> organizations,
            @RequestParam(required = false) Set<UUID> departments,
            @Parameter(description = "Параметры запроса") DepartmentParameters parameters,
            @RequestParam(defaultValue = "FULL") DepartmentProjection projection
    );

}
