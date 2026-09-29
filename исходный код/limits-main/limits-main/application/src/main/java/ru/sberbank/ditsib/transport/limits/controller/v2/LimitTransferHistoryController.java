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
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitTransferHistoryV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitTransferHistoryV2DTO;

import java.util.UUID;

/**
 * Controller interface for history items.
 */
@RequestMapping(value = "/history/transfer")
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
    GetLimitTransferHistoryV2DTO add(
            @Valid @RequestBody LimitTransferHistoryV2DTO limitTransferHistoryDTO,
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
    GetLimitTransferHistoryV2DTO get(@PathVariable("limittransferhistoryId") @NotNull UUID limittransferhistoryId);
    
    /**
     * Get all history items.
     *
     * @return list of history items.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех движений ДС", description = "Получение всех движений ДС")
    Page<GetLimitTransferHistoryV2DTO> getAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "direction", defaultValue = "ASC") @NotNull Sort.Direction direction,
            @RequestParam(value = "limitId", required = false) UUID limitId,
            @RequestParam(value = "year", required = false) Integer year
    );
}
