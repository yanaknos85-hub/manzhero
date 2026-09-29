package ru.sberbank.ditsib.transport.limits.controller.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitSharingPercentsV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitSharingPercentsV2DTO;

import java.util.UUID;

/**
 * Controller interface for limit sharings.
 */
@RequestMapping(value = "/sharings/percents")
@Tag(name = "Процентное распределение лимита", description = "Набор операций для процентного распределения")
public interface LimitSharingPercentsController {
    
    /**
     * Add new limit sharing Percents.
     *
     * @param limitSharingPercentsDTO new limit sharing Percents data.
     *
     * @return added limit sharing Percents.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление нового процентного распределения", description = "Добавление нового процентного " +
                                                                                      "распределения")
    GetLimitSharingPercentsV2DTO add(
            @Valid @RequestBody LimitSharingPercentsV2DTO limitSharingPercentsDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                  );
    
    /**
     * Edit limit sharing.
     *
     * @param limitSharingPercentsId new data of limit sharing Percents.
     * @param limitSharingPercentsDTO new data of limit sharing Percents.
     */
    @PutMapping(value = "{limitSharingPercentsId}", consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение данных процентного распределения", description = "Изменение данных процентного " +
                                                                                     "распределения")
    void edit(
            @PathVariable("limitSharingPercentsId") UUID limitSharingPercentsId,
            @Valid @RequestBody LimitSharingPercentsV2DTO limitSharingPercentsDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
             );
    
    /**
     * Delete limit sharing.
     *
     * @param limitSharingPercentsId ID of limit sharing to delete.
     */
    @DeleteMapping(value = "{limitSharingPercentsId}")
    @Operation(summary = "Удаление данных процентного распределения", description = "Удаление данных процентного " +
                                                                                    "распределения")
    void delete(
            @PathVariable("limitSharingPercentsId") UUID limitSharingPercentsId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
               );
    
    /**
     * Get limit sharing with ID.
     *
     * @param limitSharingPercentsId ID of limit sharing to get.
     *
     * @return limit sharing.
     */
    @GetMapping(value = "{limitSharingPercentsId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение данных процентного распределения", description = "Получение данных процентного " +
                                                                                     "распределения")
    GetLimitSharingPercentsV2DTO get(@PathVariable("limitSharingPercentsId") @NotNull UUID limitSharingPercentsId);
    
    /**
     * Get all limit sharings.
     *
     * @return list of limit sharings.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех процентных распределений", description = "Получение всех процентных " +
                                                                                  "распределения")
    Page<GetLimitSharingPercentsV2DTO> getAll(
            @RequestParam(value = "page", defaultValue = "0") @NotNull Integer page,
            @RequestParam(value = "size", defaultValue = "20") @NotNull Integer size,
            @RequestParam(value = "direction", defaultValue = "ASC") @NotNull Sort.Direction direction,
            @RequestParam(value = "limitId", required = false) UUID limitId
    );
}
