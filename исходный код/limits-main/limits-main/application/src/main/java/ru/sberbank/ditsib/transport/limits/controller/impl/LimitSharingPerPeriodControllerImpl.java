package ru.sberbank.ditsib.transport.limits.controller.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.limits.controller.LimitSharingPerPeriodController;
import ru.sberbank.ditsib.transport.limits.controller.v2.LimitSharingController;
import ru.sberbank.ditsib.transport.limits.mapper.LimitSharingMapper;
import ru.sberbank.ditsib.transport.limits.mapper.VersionConverter;
import ru.sberbank.ditsib.transport.limits.model.GetLimitSharingPerPeriodDTO;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of limit controller service.
 */
@RestController
@Scope("request")
class LimitSharingPerPeriodControllerImpl extends BaseController implements LimitSharingPerPeriodController {

    private final LimitSharingController controller;

    private final VersionConverter versionConverter;

    public LimitSharingPerPeriodControllerImpl(LimitSharingMapper limitSharingMapper, LimitSharingController controller, VersionConverter versionConverter) {
        super(limitSharingMapper);
        this.controller = controller;
        this.versionConverter = versionConverter;
    }

    @Override
    public List<GetLimitSharingPerPeriodDTO> getByLimitSharing(UUID limitSharingId) {
        return controller.get(limitSharingId).sharings().stream().map(versionConverter::toV1).toList();
    }
}