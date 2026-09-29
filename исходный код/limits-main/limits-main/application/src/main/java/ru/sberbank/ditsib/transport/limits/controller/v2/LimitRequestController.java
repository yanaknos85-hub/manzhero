package ru.sberbank.ditsib.transport.limits.controller.v2;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus;
import ru.sberbank.ditsib.transport.limits.dto.EmployeeState;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitRequestV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitRequestsStatsV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitRequestApproveV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitRequestCancelV2DTO;

import java.util.List;
import java.util.UUID;

@RequestMapping(value = "/requests", headers = "X-Version=2")
public interface LimitRequestController {

    /**
     * Get request with ID.
     *
     * @param requestId ID of request to get.
     * @return request.
     */
    @GetMapping(value = "/{requestId:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    GetLimitRequestV2DTO getRequest(@PathVariable("requestId") @NotNull UUID requestId);

    /**
     * Get all requests.
     *
     * @return list of requests.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Page<GetLimitRequestV2DTO> getRequests(
            @RequestParam(name = "status", required = false) LimitRequestStatus status,
            @RequestParam(name = "approvalState", required = false) ApprovalState approvalState,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "transportType", required = false) TransportTypeEnum transportType,
            @RequestParam(name = "myState", required = false) EmployeeState myState,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "20") int size,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

    /**
     * Получение статистики количества заявок по типу транспорта.
     *
     * @param active признак активности.
     * @return статистика.
     */
    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    List<GetLimitRequestsStatsV2DTO> getRequestsByTransportTypeStats(
            @RequestParam(name = "active") Boolean active,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

    /**
     * Cancel request.
     *
     * @param data data
     */
    @PostMapping(value = "/{requestId:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/cancel")
    GetLimitRequestV2DTO cancelRequest(  // only for author
                                         @PathVariable("requestId") UUID requestId,
                                         @Valid @RequestBody LimitRequestCancelV2DTO data,
                                         @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

    /**
     * Cancel request.
     *
     * @param data data
     */
    @PostMapping(value = "/{requestId:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/approve")
    GetLimitRequestV2DTO approveRequest(   // only for approver
                                           @PathVariable("requestId") UUID requestId,
                                           @Valid @RequestBody LimitRequestApproveV2DTO data,
                                           @Parameter(hidden = true) JwtAuthenticationToken authentication
    );
}
