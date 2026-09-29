package ru.sberbank.ditsib.transport.approvals.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sberbank.ditsib.transport.approvals.controller.FinalTripApproveController;
import ru.sberbank.ditsib.transport.approvals.database.model.FinalTripApproval;
import ru.sberbank.ditsib.transport.approvals.dto.CancelDTO;
import ru.sberbank.ditsib.transport.approvals.dto.TripApproveDTO;
import ru.sberbank.ditsib.transport.approvals.services.ApproveControllerService;
import ru.sberbank.ditsib.transport.approvals.services.ApproveService;
import ru.sberbank.ditsib.transport.approvals.services.EmployeeService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;

import java.util.Collection;
import java.util.UUID;

/**
 * Implementation of trip controller service.
 */
@RequiredArgsConstructor
@RestController
@E2EController
@Slf4j
public class FinalTripApproveControllerImpl implements FinalTripApproveController {

    private final ApproveService<FinalTripApproval> tripApproveService;
    private final ApproveControllerService controllerService;
    private final EmployeeService employeeService;

    @Override
    public void approve(UUID approveId) {
        var userId = getUserIdFromAuthentication();
        tripApproveService.approve(approveId, userId);
    }

    @Override
    public void decline(UUID approveId, CancelDTO cancelDTO) {
        var userId = getUserIdFromAuthentication();
        tripApproveService.decline(approveId, cancelDTO.getReason(), userId);
    }

    @Override
    public Collection<TripApproveDTO> getActiveApprovals(@E2EUser("principal") JwtAuthenticationToken authentication) {
        var userId = getUserIdFromAuthentication();
        UUID departmentId = employeeService.getDepartmentIdByUserId(userId);
        var approvals = tripApproveService.getActiveApprovalsForUser(userId).stream().map(TripApproveDTO.class::cast).toList();
        return controllerService.fillLimits(controllerService.fillTripPurpose(approvals), departmentId, "Bearer " + authentication.getToken().getTokenValue(), false);
    }

    @Override
    public Collection<TripApproveDTO> getClosedApprovals() {
        final UUID userId = getUserIdFromAuthentication();
        var approvals = tripApproveService.getClosedApprovalsForUser(userId).stream().map(TripApproveDTO.class::cast).toList();
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
