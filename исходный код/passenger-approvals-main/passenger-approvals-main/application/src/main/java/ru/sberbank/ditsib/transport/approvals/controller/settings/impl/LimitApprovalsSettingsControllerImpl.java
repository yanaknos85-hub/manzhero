package ru.sberbank.ditsib.transport.approvals.controller.settings.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sberbank.ditsib.transport.approvals.controller.settings.LimitApprovalsSettingsController;
import ru.sberbank.ditsib.transport.approvals.dto.settings.ApprovalSettingsStabSharedData;
import ru.sberbank.ditsib.transport.approvals.dto.settings.GetLimitApprovalSettingsDTO;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;

import java.util.UUID;

@RequiredArgsConstructor
@E2EController
@RestController
@Deprecated
public class LimitApprovalsSettingsControllerImpl implements LimitApprovalsSettingsController {
    
    private final ApprovalSettingsStabSharedData sharedData = new ApprovalSettingsStabSharedData();
    
    // todo стаб заменить на реализацию
    @CheckOrganizationAccess
    @Override
    public GetLimitApprovalSettingsDTO get(@Organization UUID organizationId) {
        return sharedData.createGetLimitApprovalSettingsDTO(organizationId);
    }
}