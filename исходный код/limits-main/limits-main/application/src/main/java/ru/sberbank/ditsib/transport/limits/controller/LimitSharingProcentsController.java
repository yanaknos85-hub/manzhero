package ru.sberbank.ditsib.transport.limits.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.limits.dto.GetLimitSharingPercentsDTO;
import ru.sberbank.ditsib.transport.limits.dto.LimitSharingPercentsDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Controller interface for limit sharings.
 * @deprecated use `/sharings/percents...` instead
 */
@Deprecated(since = "2023-08-01")
@RequestMapping(value = "/limitsharingprocents")
@Tag(name = "Процентное распределение лимита", description = "Набор операций для процентного распределения")
public interface LimitSharingProcentsController {
    
    /**
     * Add new limit sharing procents.
     *
     * @param limitSharingPercentsDTO new limit sharing procents data.
     *
     * @return added limit sharing procents.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление нового процентного распределения", description = "Добавление нового процентного " +
                                                                                      "распределения")
    GetLimitSharingPercentsDTO add(
            @Valid @RequestBody LimitSharingPercentsDTO limitSharingPercentsDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                  );
    
    /**
     * Edit limit sharing.
     *
     * @param limitSharingProcentsId new data of limit sharing procents.
     * @param limitSharingPercentsDTO new data of limit sharing procents.
     */
    @PutMapping(value = "{limitSharingProcentsId}", consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение данных процентного распределения", description = "Изменение данных процентного " +
                                                                                     "распределения")
    void edit(
            @PathVariable("limitSharingProcentsId") UUID limitSharingProcentsId,
            @Valid @RequestBody LimitSharingPercentsDTO limitSharingPercentsDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
             );
    
    /**
     * Delete limit sharing.
     *
     * @param limitSharingProcentsId ID of limit sharing to delete.
     */
    @DeleteMapping(value = "{limitSharingProcentsId}")
    @Operation(summary = "Удаление данных процентного распределения", description = "Удаление данных процентного " +
                                                                                    "распределения")
    void delete(
            @PathVariable("limitSharingProcentsId") UUID limitSharingProcentsId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
               );
    
    /**
     * Get limit sharing with ID.
     *
     * @param limitSharingProcentsId ID of limit sharing to get.
     *
     * @return limit sharing.
     */
    @GetMapping(value = "{limitSharingProcentsId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение данных процентного распределения", description = "Получение данных процентного " +
                                                                                     "распределения")
    GetLimitSharingPercentsDTO get(@PathVariable("limitSharingProcentsId") @NotNull UUID limitSharingProcentsId);
    
    /**
     * Get all limit sharings.
     *
     * @return list of limit sharings.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех процентных распределений", description = "Получение всех процентных " +
                                                                                  "распределения")
    Collection<GetLimitSharingPercentsDTO> getAll();
    
    /**
     * Get percents sharing by limit id.
     *
     * @return percents sharing.
     */
    @GetMapping(value = "/getByLimit/{limitId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение процентного распределения по лимиту",
               description = "Получение процентного распределения по лимиту")
    List<GetLimitSharingPercentsDTO> getByLimit(@PathVariable("limitId") @NotNull UUID limitId);
}
