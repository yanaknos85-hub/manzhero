package ru.sberbank.ditsib.transport.limits.controller.v2;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitHistoryV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitTransferHistoryV2DTO;

import java.util.UUID;

@RequestMapping("/history")
public interface LimitHistoryController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Page<GetLimitTransferHistoryV2DTO> getLimitHistory(@RequestParam(value = "organizationId", required = false) UUID organizationId,
                                                       @RequestParam(value = "limitId", required = false) UUID limitId,
                                                       @RequestParam(value = "year", required = false) Integer year,
                                                       @RequestParam(value = "page", defaultValue = "0") @NotNull Integer page,
                                                       @RequestParam(value = "size", defaultValue = "20") @NotNull Integer size,
                                                       @RequestParam(value = "direction", defaultValue = "ASC") @NotNull Sort.Direction direction,
                                                       JwtAuthenticationToken jwtAuthenticationToken);


    /**
     * Get all history items.
     *
     * @return list of history items.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, headers = "X-Version=2")
    @ResponseBody
    @Operation(summary = "Получение всех движений ДС", description = "Получение всех движений ДС")
    Page<GetLimitHistoryV2DTO> getAll(
            @RequestParam(value = "limitId", required = false) UUID limitId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "direction", defaultValue = "ASC") @NotNull Sort.Direction direction
            );


}
