package ru.sberbank.ditsib.transport.limits.controller.v2;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.limits.dto.v2.DepLimitRequestV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitRequestV2DTO;

import java.util.UUID;

/**
 * Controller for working with limit requests.
 */
@RequestMapping(value = "/requests/department")
public interface DepLimitRequestController {
    
    /**
     * Add a new fill request.
     *
     * @param newRequest new request data.
     * @param authentication authentication data.
     *
     * @return added request.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    GetLimitRequestV2DTO addRequest(
            @Valid @RequestBody DepLimitRequestV2DTO newRequest,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                 );
    
    /**
     * Edit request.
     *
     * @param requestId ID of request..
     * @param newData new data of request.
     * @param authentication authentication data.
     */
    @PutMapping(value = "{requestId}", consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    void editRequest(
            @PathVariable("requestId") UUID requestId,
            @Valid @RequestBody DepLimitRequestV2DTO newData,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                    );
}
