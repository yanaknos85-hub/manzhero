package ru.sberbank.ditsib.transport.limits.controller.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitSharingV2DTO;

import java.util.UUID;

/**
 * Controller interface for limit sharings.
 */
@RequestMapping(value = "/sharings")
@Tag(name = "Распределение лимита", description = "Набор операций для распределения лимита")
public interface LimitSharingController {
    
    /**
     * Get limit sharing with ID.
     *
     * @param limitSharingId ID of limit sharing to get.
     *
     * @return limit sharing.
     */
    @GetMapping(value = "{limitSharingId:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение данных распределения", description = "Получение данных распределения")
    GetLimitSharingV2DTO get(@PathVariable("limitSharingId") @NotNull UUID limitSharingId);
    
    /**
     * Get all limit sharings.
     *
     * @return list of limit sharings.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех распределений", description = "Получение всех распределения")
    Page<GetLimitSharingV2DTO> getAll(
            @RequestParam(value = "page", defaultValue = "0") @NotNull Integer page,
            @RequestParam(value = "size", defaultValue = "20") @NotNull Integer size,
            @RequestParam(value = "direction", defaultValue = "ASC") @NotNull Sort.Direction direction,
            @RequestParam(value = "limitId", required = false) UUID limitId
    );
}
