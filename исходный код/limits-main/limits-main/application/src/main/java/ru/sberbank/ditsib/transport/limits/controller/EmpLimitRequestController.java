package ru.sberbank.ditsib.transport.limits.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.limits.dto.EmpLimitRequestDTO;
import ru.sberbank.ditsib.transport.limits.dto.GetLimitRequestDTO;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * Controller for working with limit requests.
 * @deprecated use LimitRequestController instead
 */
@Deprecated(since = "2023-07-31")
@RequestMapping(value = "/requests/emp")
@Tag(name = "Заявки на пополнение личного лимита",
     description = "Заявки на пополнение личного лимита")
public interface EmpLimitRequestController {
    
    /**
     * Add a new personal request.
     *
     * @param newRequest new personal request data.
     *
     * @return added personal request.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление", description = "Добавление новой заявки на лимит")
    GetLimitRequestDTO addRequest(
            @Valid @RequestBody EmpLimitRequestDTO newRequest,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                 );
    
    /**
     * Edit request.
     *
     * @param newData new data of request.
     */
    @PutMapping(value = "{requestId}", consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных заявки на лимит")
    void editRequest(
            @PathVariable("requestId") UUID requestId,
            @Valid @RequestBody EmpLimitRequestDTO newData,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                    );
}
