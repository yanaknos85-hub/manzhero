package ru.sberbank.ditsib.transport.limits.controller.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.limits.controller.EmpLimitRequestController;
import ru.sberbank.ditsib.transport.limits.controller.v2.EmpLimitRequestV2Controller;
import ru.sberbank.ditsib.transport.limits.dto.EmpLimitRequestDTO;
import ru.sberbank.ditsib.transport.limits.dto.GetLimitRequestDTO;
import ru.sberbank.ditsib.transport.limits.mapper.LimitSharingMapper;
import ru.sberbank.ditsib.transport.limits.mapper.VersionConverter;

import java.util.UUID;

/**
 * Implementation of personal request controller service.
 */
@Slf4j
@RestController
@Scope("request")
class EmpLimitRequestControllerImpl extends BaseController implements EmpLimitRequestController {

    private final EmpLimitRequestV2Controller controller;

    private final VersionConverter versionConverter;

    public EmpLimitRequestControllerImpl(LimitSharingMapper limitSharingMapper, EmpLimitRequestV2Controller controller, VersionConverter versionConverter) {
        super(limitSharingMapper);
        this.controller = controller;
        this.versionConverter = versionConverter;
    }

    @Override
    public GetLimitRequestDTO addRequest(
            EmpLimitRequestDTO newRequestDTO, JwtAuthenticationToken authentication
                                        ) {
        return versionConverter.toV1(controller.addRequest(versionConverter.toV2(newRequestDTO), authentication));
    }
    
    @Override
    public void editRequest(
            UUID requestId, EmpLimitRequestDTO limitRequestDTO, JwtAuthenticationToken authentication
                           ) {
        controller.editRequest(requestId, versionConverter.toV2(limitRequestDTO), authentication);
    }
}
