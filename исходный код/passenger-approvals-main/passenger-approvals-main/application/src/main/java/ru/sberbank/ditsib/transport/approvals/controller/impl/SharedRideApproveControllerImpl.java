package ru.sberbank.ditsib.transport.approvals.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sberbank.ditsib.transport.approvals.controller.SharedRideApproveController;
import ru.sberbank.ditsib.transport.approvals.database.model.SharedRideJoinApproval;
import ru.sberbank.ditsib.transport.approvals.dto.SharedRideApproveDTO;
import ru.sberbank.ditsib.transport.approvals.services.ApproveControllerService;
import ru.sberbank.ditsib.transport.approvals.services.ApproveService;
import ru.sberbank.ditsib.transport.approvals.services.EmployeeService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;

import java.util.Collection;
import java.util.UUID;

/**
 * Implementation of shared ride controller service.
 */
@RequiredArgsConstructor
@E2EController
@RestController
@Slf4j
public class SharedRideApproveControllerImpl implements SharedRideApproveController {
    
    private final ApproveService<SharedRideJoinApproval> tripApproveService;
    private final ApproveControllerService controllerService;
    private final EmployeeService employeeService;
    
    @Override
    public void approve(UUID approveId) {
        var userId = getUserIdFromAuthentication();
        tripApproveService.approve(approveId, userId);
    }
    
    @Override
    public void decline(UUID approveId) {
        var userId = getUserIdFromAuthentication();
        tripApproveService.decline(approveId, null, userId);
    }
    
    @Override
    public Collection<SharedRideApproveDTO> getActiveApprovals(@E2EUser("principal") JwtAuthenticationToken authentication) {
        var userId = getUserIdFromAuthentication();
        UUID departmentId = employeeService.getDepartmentIdByUserId(userId);
        var approvals = tripApproveService.getActiveApprovalsForUser(userId).stream().map(SharedRideApproveDTO.class::cast).toList();
        return controllerService.fillLimits(controllerService.fillTripPurpose(approvals), departmentId,
                                            "Bearer " + authentication.getToken().getTokenValue(), false);
    }
    
    @Override
    public Collection<SharedRideApproveDTO> getClosedApprovals() {
        final UUID userId = getUserIdFromAuthentication();
        var approvals = tripApproveService.getClosedApprovalsForUser(userId).stream().map(SharedRideApproveDTO.class::cast).toList();
        return controllerService.fillTripPurpose(approvals);
    }
    
    /**
     * Получить ID пользователя из параметров аутентификации
     *
     * @return ID пользователя
     */
    private UUID getUserIdFromAuthentication() {
        return ControllerUtils.currentUser();
    }
}
