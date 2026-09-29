package ru.sberbank.ditsib.transport.approvals.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.approvals.controller.UpdateTripRequestApproveController;
import ru.sberbank.ditsib.transport.approvals.database.model.Status;
import ru.sberbank.ditsib.transport.approvals.database.model.UpdateTripRequestApproval;
import ru.sberbank.ditsib.transport.approvals.dto.CancelDTO;
import ru.sberbank.ditsib.transport.approvals.dto.TripApproveDTO;
import ru.sberbank.ditsib.transport.approvals.mappers.TripApprovalMapper;
import ru.sberbank.ditsib.transport.approvals.services.ApproveControllerService;
import ru.sberbank.ditsib.transport.approvals.services.ApproveService;
import ru.sberbank.ditsib.transport.approvals.services.EmployeeService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of update trip controller service.
 */
@RequiredArgsConstructor
@E2EController
@RestController
@Slf4j
public class UpdateTripRequestApproveControllerImpl implements UpdateTripRequestApproveController {
    
    private final ApproveService<UpdateTripRequestApproval> tripApproveService;
    private final TripApprovalMapper tripApprovalMapper;
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
        var departmentId = employeeService.getDepartmentIdByUserId(userId);
        var approvals = tripApproveService.getActiveApprovalsForUser(userId).stream().map(TripApproveDTO.class::cast).toList();
        return controllerService.fillLimits(controllerService.fillTripPurpose(approvals), departmentId,
                                            "Bearer " + authentication.getToken().getTokenValue(), false);
    }
    
    @Override
    public Collection<TripApproveDTO> getClosedApprovals() {
        var userId = getUserIdFromAuthentication();
        var approvals = tripApproveService.getClosedApprovalsForUser(userId).stream().map(TripApproveDTO.class::cast).toList();
        return controllerService.fillTripPurpose(approvals);
    }
    
    @Override
    public TripApproveDTO getApprovalByRequestId(UUID requestId) {
        var approvals = tripApproveService.findApprovalByActionIdAndStatuses(requestId, Set.of(Status.NEW, Status.EDITED));
        if (approvals.isEmpty()) {
            throw new EntityNotFoundException(UpdateTripRequestApproval.class, Map.of("requestId", requestId));
        }
        return tripApprovalMapper.toApproveDTO(approvals.get(0));
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
