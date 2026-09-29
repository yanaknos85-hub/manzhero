package ru.sberbank.ditsib.transport.limits.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.limits.dto.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Controller interface for history items.
 *
 * @deprecated use `/history/transfer/...` with header `X-Version=2` or `/economy/...` instead
 */
@Deprecated(since = "2023-07-31")
@RequestMapping(value = "/limittransferhistory")
@Tag(name = "История движения денежных средств", description = "История движения денежных средств")
public interface LimitTransferHistoryController {
    
    /**
     * Add new limit transfer history.
     *
     * @param limitTransferHistoryDTO new limit transfer history.
     *
     * @return added limit transfer history.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление истории трансферов", description = "Добавление нового истории трансферов")
    GetLimitTransferHistoryDTO add(
            @Valid @RequestBody LimitTransferHistoryDTO limitTransferHistoryDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                  );
    
    /**
     * Delete limit transfer history.
     *
     * @param limitTransferHistoryId ID of limit transfer history to delete.
     */
    @DeleteMapping(value = "{limitTransferHistoryId}")
    @Operation(summary = "Удаление истории трансферов", description = "Удаление истории трансферов")
    void delete(
            @PathVariable("limitTransferHistoryId") UUID limitTransferHistoryId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
               );
    
    /**
     * Get history item with ID.
     *
     * @param limittransferhistoryId ID of history item to get.
     *
     * @return history item.
     */
    @GetMapping(value = "{limittransferhistoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение движения ДС по ID", description = "Получение движения ДС по ID")
    GetLimitTransferHistoryDTO get(@PathVariable("limittransferhistoryId") @NotNull UUID limittransferhistoryId);
    
    /**
     * Get all history items.
     *
     * @return list of history items.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех движений ДС", description = "Получение всех движений ДС")
    Collection<GetLimitTransferHistoryDTO> getAll();
    
    
    /**
     * Get economy stats.
     *
     * @return list of economy stats.
     */
    @GetMapping(value = "/economy/to/year/{year}/limit/{limitId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение экономии за год", description = "Получение экономии за год")
    Collection<EconomyDTO> getTransfersToEconomy(@PathVariable("year") @NotNull Integer year,
                                                @PathVariable("limitId") @NotNull UUID limitId);
    
    /**
     * Get economy stats.
     *
     * @return list of economy stats.
     */
    @GetMapping(value = "/economy/from/year/{year}/limit/{limitId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение экономии за год", description = "Получение экономии за год")
    Collection<GetLimitTransferHistoryDTO> getTransfersFromEconomy(@PathVariable("year") @NotNull Integer year,
                                                             @PathVariable("limitId") @NotNull UUID limitId);
    
    /**
     * Get transfers by limit.
     *
     * @param limitId ID of limit.
     *
     * @return transfers.
     */
    @GetMapping(value = "/getByLimit/{limitId}/{year}/{maxRecords}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Движение ДС по лимиту", description = "Получение движений ДС по лимиту")
    List<GetLimitTransferHistoryDTO> getTransfersByLimit(@PathVariable("limitId") @NotNull UUID limitId,
                                                                   @PathVariable("year") @NotNull Integer year,
                                                                   @PathVariable("maxRecords") @NotNull Integer maxRecords);
}
