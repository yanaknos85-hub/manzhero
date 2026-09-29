package ru.sberbank.ditsib.transport.limits.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.limits.model.GetLimitSharingPerPeriodDTO;

import java.util.List;
import java.util.UUID;

/**
 * Controller interface for limit sharings per period.
 *
 * @deprecated use `GET /sharings/{limitSharingId}` with header `X-Version=2` instead
 */
@Deprecated(since = "2023-07-31")
@RequestMapping(value = "/limitsharingperperiod")
@Tag(name = "Распределение лимита по периодам", description = "Набор операций для распределения лимита по периодам")
public interface LimitSharingPerPeriodController {
    
    /**
     * Get limit sharings per period.
     *
     * @return list of limit sharings per period.
     */
    @GetMapping(value = "/{limitSharingId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение данных распределения по периодам",
               description = "Получение данных распределения по периодам")
    List<GetLimitSharingPerPeriodDTO> getByLimitSharing(@PathVariable("limitSharingId") @NotNull UUID limitSharingId);
}
