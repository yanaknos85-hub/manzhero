package ru.sberbank.ditsib.transport.limits.controller.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.limits.dto.v2.EmpLimitRequestV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitRequestV2DTO;

import java.util.UUID;

/**
 * Controller for working with limit requests.
 */
@RequestMapping(value = "/requests/employee")
@Tag(name = "Заявки на пополнение личного лимита",
     description = "Заявки на пополнение личного лимита")
public interface EmpLimitRequestV2Controller {
    
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
    GetLimitRequestV2DTO addRequest(
            @Valid @RequestBody EmpLimitRequestV2DTO newRequest,
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
            @Valid @RequestBody EmpLimitRequestV2DTO newData,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                    );
}
