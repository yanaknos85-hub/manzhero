package ru.sber.transport.tariff_fleet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sber.transport.tariff_fleet.dto.EdfOperatorDto;

import java.util.Set;

/**
 * Контроллер по операторам ЭДО
 */
@RequestMapping("edf-operators")
@Tag(name = "Оператор ЭДО", description = "Контроллер по операторам ЭДО")
public interface EdfOperatorController {
    
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение", description = "Получение договора")
    Set<EdfOperatorDto> getAllActive();
}
