package ru.sberbank.ditsib.transport.limits.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sberbank.ditsib.transport.limits.dto.GetLimitHistoryDTO;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Controller interface for history items.
 *
 * @deprecated use `GET /history` with header `X-Version: 2`.
 */
@Deprecated(since = "2023-08-17")
@RequestMapping(value = "/limithistory")
@Tag(name = "История движения денежных средств", description = "История движения денежных средств")
public interface LimitHistoryController {
    
    /**
     * Get all history items.
     *
     * @return list of history items.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех движений ДС", description = "Получение всех движений ДС")
    Collection<GetLimitHistoryDTO> getAll();
    
    /**
     * Get transfers by limit.
     *
     * @param limitId ID of limit.
     *
     * @return transfers.
     */
    @GetMapping(value = "/getByLimit/{limitId}/maxRecords/{maxRecords}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Движение ДС по лимиту", description = "Получение движений ДС по лимиту")
    List<GetLimitHistoryDTO> getByLimit(@PathVariable("limitId") @NotNull UUID limitId,
                                                                   @PathVariable("maxRecords") @NotNull Integer maxRecords);
}
