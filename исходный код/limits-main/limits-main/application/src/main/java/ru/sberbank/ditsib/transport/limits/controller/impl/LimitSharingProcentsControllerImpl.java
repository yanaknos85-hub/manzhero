package ru.sberbank.ditsib.transport.limits.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.limits.controller.LimitSharingProcentsController;
import ru.sberbank.ditsib.transport.limits.controller.v2.LimitSharingPercentsController;
import ru.sberbank.ditsib.transport.limits.dto.GetLimitSharingPercentsDTO;
import ru.sberbank.ditsib.transport.limits.dto.LimitSharingPercentsDTO;
import ru.sberbank.ditsib.transport.limits.mapper.VersionConverter;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of limit controller service.
 */
@RequiredArgsConstructor
@RestController
@Scope("request")
class LimitSharingProcentsControllerImpl implements LimitSharingProcentsController {

    private final LimitSharingPercentsController controller;

    private final VersionConverter versionConverter;
    
    @Override
    public GetLimitSharingPercentsDTO add(
            LimitSharingPercentsDTO limitSharingPercentsDTO, JwtAuthenticationToken authentication
                                         ) {
        return versionConverter.toV1(controller.add(versionConverter.toV2(limitSharingPercentsDTO), authentication));
    }
    
    @Override
    public void edit(
            UUID limitSharingProcentsId, LimitSharingPercentsDTO limitSharingPercentsDTO, JwtAuthenticationToken authentication
                    ) {
        controller.edit(limitSharingProcentsId, versionConverter.toV2(limitSharingPercentsDTO), authentication);
    }
    
    @Override
    public void delete(UUID limitSharingProcentsId, JwtAuthenticationToken authentication) {
        controller.delete(limitSharingProcentsId, authentication);
    }
    
    @Override
    public GetLimitSharingPercentsDTO get(UUID limitSharingProcentsId) {
        return versionConverter.toV1(controller.get(limitSharingProcentsId));
    }
    
    @Override
    public List<GetLimitSharingPercentsDTO> getAll() {
        return controller.getAll(0, Integer.MAX_VALUE, Sort.Direction.ASC, null)
                .map(versionConverter::toV1).getContent();
    }
    
    @Override
    public List<GetLimitSharingPercentsDTO> getByLimit(UUID limitId) {
        return controller.getAll(0, Integer.MAX_VALUE, Sort.Direction.ASC, limitId)
                .map(versionConverter::toV1).getContent();
    }
}