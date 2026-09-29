package ru.sberbank.ditsib.transport.limits.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus;
import ru.sberbank.ditsib.transport.limits.dto.LimitRequestApproveDTO;
import ru.sberbank.ditsib.transport.limits.dto.LimitRequestCancelDTO;
import ru.sberbank.ditsib.transport.limits.dto.GetLimitRequestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.UUID;

/**
 * Controller for working with limit requests.
 */
@RequestMapping(value = "/requests")
@Tag(name = "Заявки на лимит", description = "Заявки на лимит")
public interface LimitRequestController {

    /**
     * Get request with ID.
     *
     * @param requestId ID of request to get.
     * @return request.
     * @deprecated use `GET /requests?requestId={requestId}` with header `X-Version=2` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = "{requestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных заявки на лимит")
    GetLimitRequestDTO getRequest(@PathVariable("requestId") @NotNull UUID requestId);

    /**
     * Get all requests.
     *
     * @return list of requests.
     * @deprecated use `GET /requests` with header `X-Version=2` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех заявок", description = "Получение всех заявок на лимит")
    Collection<GetLimitRequestDTO> getRequests(JwtAuthenticationToken token);

    /**
     * Get all requests.
     *
     * @return list of requests.
     * @deprecated use `GET /requests?status={status}` with header `X-Version=2` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = "/filter/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение заявок с фильтром", description = "Получение заявок на лимит с фильтром")
    Collection<GetLimitRequestDTO> getRequestsFiltered(@PathVariable("status") LimitRequestStatus status, JwtAuthenticationToken token);

    /**
     * Cancel request.
     *
     * @param data data
     * @deprecated use `PUT /requests/{requestId}/cancel` with header `X-Version=2` instead
     */
    @Deprecated(since = "2023-07-31")
    @PutMapping(value = "/cancel")
    @Operation(summary = "Отмена", description = "Отмена доступной для редактирования заявки на лимит")
    GetLimitRequestDTO cancelRequest(  // only for author
                                       @Valid @RequestBody LimitRequestCancelDTO data,
                                       @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

    /**
     * Cancel request.
     *
     * @param data data
     * @deprecated use `PUT /requests/{requestId}/approve` with header `X-Version=2` instead
     */
    @Deprecated(since = "2023-07-31")
    @PutMapping(value = "/approve")
    @Operation(summary = "Отмена", description = "Подтверждение заявки на лимит")
    GetLimitRequestDTO approveRequest(   // only for approver
                                         @Valid @RequestBody LimitRequestApproveDTO data,
                                         @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

    /**
     * Get requests for author.
     *
     * @return requests.
     * @deprecated use `GET /requests?my=true&amp;myState=AUTHOR` with header `X-Version=2` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = "/getByAuthor", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение своих заявок",
            description = "Получение своих заявок")
    Collection<GetLimitRequestDTO> getRequestsByAuthor(@Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Get requests for approver.
     *
     * @return requests.
     * @deprecated use `GET /requests?my=true&amp;myState=APPROVER` with header `X-Version=2` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = "/getByApprover", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение заявок на согласование",
            description = "Получение заявок на согласование")
    Collection<GetLimitRequestDTO> getRequestsByApprover(@Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Get active requests for approver.
     *
     * @return requests.
     * @deprecated use `GET /requests?my=true&amp;myState=APPROVER&amp;statuses[]=AWAITING_APPROVAL` with header `X-Version=2` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = "/getActiveByApprover", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение заявок на согласование с пейджингом",
            description = "Получение заявок на согласование с пейджингом")
    Page<GetLimitRequestDTO> getActiveRequestsByApprover(@Parameter(hidden = true) JwtAuthenticationToken authentication,
                                                         @PageableDefault(size = 20, direction =
                                                                 Sort.Direction.ASC) Pageable pageable,
                                                         @RequestParam(name = "transportType", required = false) TransportTypeEnum transportType);

    /**
     * Get old requests for approver.
     *
     * @return requests.
     * @deprecated use `GET /requests?my=true&amp;myState=APPROVER&amp;statuses[]=APPROVED&amp;statuses[]=DECLINED` with header `X-Version=2` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = "/getOldByApprover", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение старых заявок на согласование с пейджингом",
            description = "Получение старых заявок на согласование с пейджингом")
    Page<GetLimitRequestDTO> getOldRequestsByApprover(@Parameter(hidden = true) JwtAuthenticationToken authentication,
                                                      @PageableDefault(size = 20, direction =
                                                              Sort.Direction.ASC) Pageable pageable,
                                                      @RequestParam(name = "transportType", required = false) TransportTypeEnum transportType);
}
