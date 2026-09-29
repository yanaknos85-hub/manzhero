package ru.sberbank.ditsib.transport.limits.controller.v2.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.exceptions.UnauthorizedException;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sberbank.ditsib.transport.limits.controller.v2.LimitHistoryController;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitHistoryV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitTransferHistoryV2DTO;
import ru.sberbank.ditsib.transport.limits.mapper.LimitObjectMapper;
import ru.sberbank.ditsib.transport.limits.mapper.LimitTransferHistoryMapper;
import ru.sberbank.ditsib.transport.limits.service.LimitHistoryService;
import ru.sberbank.ditsib.transport.limits.service.LimitTransferHistoryService;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@Scope("request")
class LimitHistoryControllerV2Impl implements LimitHistoryController {

    private final LimitTransferHistoryService limitTransferHistoryService;

    private final EmployeeOrganizationFunction employeeOrganizationFunction;

    private final LimitTransferHistoryMapper mapper;

    private final LimitHistoryService limitHistoryService;

    private final LimitObjectMapper limitObjectMapper;

    @Override
    public Page<GetLimitTransferHistoryV2DTO> getLimitHistory(UUID organizationId, UUID limitId, Integer year, Integer page, Integer size, Sort.Direction direction, JwtAuthenticationToken jwtAuthenticationToken) {
        var token = jwtAuthenticationToken.getToken();
        UUID t = UUID.fromString(token.getId());
        if (Boolean.FALSE.equals(token.getClaimAsBoolean("data_master"))) {
            if (organizationId == null) {
                organizationId = employeeOrganizationFunction.apply(t);
            } else {
                throw new UnauthorizedException(t);
            }
        }
        var list = limitTransferHistoryService.getAll(organizationId, limitId, year, page, size, direction);
        return list.map(mapper::toV2Dto);
    }

    @Override
    public Page<GetLimitHistoryV2DTO> getAll(UUID limitId, int page, int size, Sort.Direction direction) {
        var list = limitHistoryService.getAll(limitId, page, size, direction);
        return list.map(limitObjectMapper::convertToV2Dto);
    }

}
