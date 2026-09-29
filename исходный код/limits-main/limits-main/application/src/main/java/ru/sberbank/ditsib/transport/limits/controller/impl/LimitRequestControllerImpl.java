package ru.sberbank.ditsib.transport.limits.controller.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus;
import ru.sberbank.ditsib.transport.limits.controller.LimitRequestController;
import ru.sberbank.ditsib.transport.limits.dto.EmployeeState;
import ru.sberbank.ditsib.transport.limits.dto.GetLimitRequestDTO;
import ru.sberbank.ditsib.transport.limits.dto.LimitRequestApproveDTO;
import ru.sberbank.ditsib.transport.limits.dto.LimitRequestCancelDTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitRequestApproveV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitRequestCancelV2DTO;
import ru.sberbank.ditsib.transport.limits.mapper.LimitSharingMapper;
import ru.sberbank.ditsib.transport.limits.mapper.VersionConverter;

import java.util.Collection;
import java.util.List;
import java.util.UUID;


/**
 * Implementation of personal request controller service.
 */
@RestController
@Transactional
@Scope("request")
class LimitRequestControllerImpl extends BaseController implements LimitRequestController {

    private final ru.sberbank.ditsib.transport.limits.controller.v2.LimitRequestController controller;

    private final VersionConverter converter;

    public LimitRequestControllerImpl(LimitSharingMapper limitSharingMapper, ru.sberbank.ditsib.transport.limits.controller.v2.LimitRequestController controller, VersionConverter converter) {
        super(limitSharingMapper);
        this.controller = controller;
        this.converter = converter;
    }

    @Override
    public GetLimitRequestDTO getRequest(UUID requestId) {
        return converter.toV1(controller.getRequest(requestId));
    }
    
    @Override
    public List<GetLimitRequestDTO> getRequests(JwtAuthenticationToken token) {
        return controller.getRequests(null, null, null, null, null, 0, Integer.MAX_VALUE, token)
                .map(converter::toV1)
                .getContent();
    }
    
    @Override
    public List<GetLimitRequestDTO> getRequestsFiltered(LimitRequestStatus status, JwtAuthenticationToken token) {
        return controller.getRequests(status, null, null, null, null, 0, Integer.MAX_VALUE, token)
                .map(converter::toV1)
                .getContent();
    }
    
    @Override
    public GetLimitRequestDTO cancelRequest(LimitRequestCancelDTO dto, JwtAuthenticationToken authentication) {
        return converter.toV1(controller.cancelRequest(dto.getRequestId(), new LimitRequestCancelV2DTO(dto.getDescription()), authentication));
    }
    
    @Override
    public GetLimitRequestDTO approveRequest(LimitRequestApproveDTO dto, JwtAuthenticationToken authentication) {
        return converter.toV1(controller.approveRequest(dto.getRequestId(), new LimitRequestApproveV2DTO(dto.getSum(), dto.getApprovalState()), authentication));
    }
    
    @Override
    public Collection<GetLimitRequestDTO> getRequestsByAuthor(JwtAuthenticationToken authentication) {
        return controller.getRequests(null, null, null, null, EmployeeState.AUTHOR, 0, Integer.MAX_VALUE, authentication)
                .map(converter::toV1)
                .getContent();
    }
    
    @Override
    public Collection<GetLimitRequestDTO> getRequestsByApprover(JwtAuthenticationToken authentication) {
        return controller.getRequests(null, null, null, null, EmployeeState.APPROVER, 0, Integer.MAX_VALUE, authentication)
                .map(converter::toV1)
                .getContent();
    }

    @Override
    public Page<GetLimitRequestDTO> getActiveRequestsByApprover(JwtAuthenticationToken authentication,
                                                                Pageable pageable,
                                                                TransportTypeEnum transportType) {
        return controller.getRequests(null, null, true, transportType, EmployeeState.APPROVER, pageable.getPageNumber(), pageable.getPageSize(), authentication)
                .map(converter::toV1);
    }

    @Override
    public Page<GetLimitRequestDTO> getOldRequestsByApprover(JwtAuthenticationToken authentication,
                                                             Pageable pageable,
                                                             TransportTypeEnum transportType) {
        return controller.getRequests(null, null, false, transportType, EmployeeState.APPROVER, pageable.getPageNumber(), pageable.getPageSize(), authentication)
                .map(converter::toV1);
    }
}
