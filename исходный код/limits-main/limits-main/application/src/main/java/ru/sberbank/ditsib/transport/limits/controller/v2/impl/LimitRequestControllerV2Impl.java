package ru.sberbank.ditsib.transport.limits.controller.v2.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus;
import ru.sberbank.ditsib.transport.limits.controller.v2.LimitRequestController;
import ru.sberbank.ditsib.transport.limits.dto.EmployeeState;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitRequestV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitRequestsStatsV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitRequestApproveV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitRequestCancelV2DTO;
import ru.sberbank.ditsib.transport.limits.exceptions.ActionNotAuthorizedException;
import ru.sberbank.ditsib.transport.limits.mapper.LimitRequestMapper;
import ru.sberbank.ditsib.transport.limits.model.limit.Approver;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitRequest;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;
import ru.sberbank.ditsib.transport.limits.service.LimitRequestService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@Scope("request")
class LimitRequestControllerV2Impl extends BaseControllerImpl implements LimitRequestController {

    private final LimitRequestService limitRequestService;

    private final LimitRequestMapper limitRequestMapper;

    private final EmployeeService employeeService;

    @Override
    public GetLimitRequestV2DTO getRequest(UUID requestId) {
        var limitRequest = limitRequestService.get(requestId).orElseThrow(() -> new EntityNotFoundException(LimitRequest.class, requestId));
        return limitRequestMapper.toV2Dto(limitRequest);
    }

    @Override
    public Page<GetLimitRequestV2DTO> getRequests(LimitRequestStatus status, ApprovalState approvalState, Boolean active, TransportTypeEnum transportType, EmployeeState myState,
                                                  int page,
                                                  int size,
                                                  JwtAuthenticationToken authentication) {
        var employee = getEmployee(employeeService, authentication);
        return limitRequestService.getAll(status, approvalState, active, transportType, myState, page, size, employee)
                .map(limitRequestMapper::toV2Dto);
    }

    @Override
    public List<GetLimitRequestsStatsV2DTO> getRequestsByTransportTypeStats(Boolean active, JwtAuthenticationToken authentication) {
        var employee = getEmployee(employeeService, authentication);
        return limitRequestService.getStatistic(active, employee);
    }

    @Override
    public GetLimitRequestV2DTO cancelRequest(
            UUID requestId, LimitRequestCancelV2DTO data, JwtAuthenticationToken authentication
    ) {
        var limitRequest = limitRequestService.get(requestId)
                .orElseThrow(() -> new EntityNotFoundException(LimitRequest.class, requestId));
        UUID userId = UUID.fromString(authentication.getToken().getId());
        if (!userId.equals(limitRequest.getAuthor().getUserId())) {
            throw new ActionNotAuthorizedException("Залогиненный пользователь не автор заявки");
        }
        return limitRequestMapper.toV2Dto(limitRequestService.cancel(requestId, data.description()));
    }

    @Override
    public GetLimitRequestV2DTO approveRequest(
            UUID requestId, LimitRequestApproveV2DTO data, JwtAuthenticationToken authentication
    ) {
        var limitRequest = limitRequestService.get(requestId)
                .orElseThrow(() -> new EntityNotFoundException(LimitRequest.class, requestId));
        var employee = getEmployee(employeeService, authentication);

        // get approvers set
        var approverIdSet = limitRequest.getApproverList().stream()
                .map(e -> e.getEmployee().getId())
                .collect(Collectors.toSet());
        // check employee is in approvers list
        if (!approverIdSet.contains(employee.getId())) {
            throw new ActionNotAuthorizedException("Залогиненный пользователь не согласующее лицо");
        }
        // save approval info for approver
        limitRequest = limitRequestService.approve(data, limitRequest, employee);

        // update request status
        var isWaiting = limitRequest.getApproverList().stream()
                .map(Approver::getApprovalState)
                .anyMatch(e -> e.equals(ApprovalState.AWAITING_APPROVAL));
        var isDeclined = limitRequest.getApproverList().stream()
                .map(Approver::getApprovalState)
                .allMatch(e -> e.equals(ApprovalState.DECLINED));
        if (!isWaiting) {
            if (isDeclined) {
                limitRequest.setStatus(LimitRequestStatus.CANCELLED);
                limitRequest.setStatusCode(LimitRequestStatus.CANCELLED_BY_APPROVERS);
            } else {
                final var totalSum = limitRequest.getApproverList().stream()
                        .map(Approver::getSum)
                        .reduce(BigDecimal::add)
                        .orElse(BigDecimal.ZERO);
                if (totalSum.compareTo(limitRequest.getSum()) == 0) {
                    limitRequest.setStatus(LimitRequestStatus.DONE_FULLY);
                } else {
                    limitRequest.setStatus(LimitRequestStatus.DONE_PARTLY);
                }
            }
            limitRequestService.save(limitRequest);
        }

        return limitRequestMapper.toV2Dto(limitRequest);
    }
}
