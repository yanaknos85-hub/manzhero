package ru.sberbank.ditsib.transport.limits.controller.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.limits.controller.DepLimitRequestController;
import ru.sberbank.ditsib.transport.limits.dto.DepLimitRequestDTO;
import ru.sberbank.ditsib.transport.limits.dto.GetLimitRequestDTO;
import ru.sberbank.ditsib.transport.limits.mapper.LimitSharingMapper;
import ru.sberbank.ditsib.transport.limits.mapper.VersionConverter;

import java.util.UUID;


/**
 * Implementation of fill request controller service.
 */
@RestController
@Scope("request")
class DepLimitRequestControllerImpl extends BaseController implements DepLimitRequestController {

    private final ru.sberbank.ditsib.transport.limits.controller.v2.DepLimitRequestController controller;

    private final VersionConverter versionConverter;

    public DepLimitRequestControllerImpl(LimitSharingMapper limitSharingMapper, ru.sberbank.ditsib.transport.limits.controller.v2.DepLimitRequestController controller, VersionConverter versionConverter) {
        super(limitSharingMapper);
        this.controller = controller;
        this.versionConverter = versionConverter;
    }

    @Override
    public GetLimitRequestDTO addRequest(
        DepLimitRequestDTO newRequestDTO, JwtAuthenticationToken authentication) {
        return versionConverter.toV1(controller.addRequest(versionConverter.toV2(newRequestDTO), authentication));
    }

    @Override
    public void editRequest(
        UUID requestId, DepLimitRequestDTO limitRequestDTO, JwtAuthenticationToken authentication
    ) {
        controller.editRequest(requestId, versionConverter.toV2(limitRequestDTO), authentication);
    }
}
