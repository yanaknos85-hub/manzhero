package ru.sberbank.ditsib.transport.approvals.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sberbank.ditsib.transport.approvals.controller.TripRequestApproveController;
import ru.sberbank.ditsib.transport.approvals.database.model.TripRequestApproval;
import ru.sberbank.ditsib.transport.approvals.dto.CancelDTO;
import ru.sberbank.ditsib.transport.approvals.dto.TripApproveDTO;
import ru.sberbank.ditsib.transport.approvals.dto.params.EmployeeSearchParams;
import ru.sberbank.ditsib.transport.approvals.services.ApproveControllerService;
import ru.sberbank.ditsib.transport.approvals.services.ApproveService;
import ru.sberbank.ditsib.transport.approvals.services.EmployeeService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of request controller service.
 */
@RequiredArgsConstructor
@E2EController
@RestController
@Slf4j
public class TripRequestApproveControllerImpl implements TripRequestApproveController {
    
    private final ApproveService<TripRequestApproval> tripApproveService;
    private final ApproveControllerService controllerService;
    private final EmployeeService employeeService;
    
    @Override
    public void approve(UUID approveId) {
        var userId = getUserIdFromAuthentication();
        tripApproveService.approve(approveId, userId);
    }
    
    @Override
    public void decline(UUID approveId, CancelDTO cancelDTO) {
        final UUID userId = getUserIdFromAuthentication();
        tripApproveService.decline(approveId, cancelDTO.getReason(), userId);
    }
    
    @Override
    public Page<TripApproveDTO> getPageActiveApprovals(@E2EUser("principal") JwtAuthenticationToken authentication,
                                                       EmployeeSearchParams employeeParams,
                                                       Pageable page) {
        var userId = getUserIdFromAuthentication();
        UUID departmentId = employeeService.getDepartmentIdByUserId(userId);
        var approvals = tripApproveService.getActiveApprovalsForUser(userId, page, employeeParams);
        
        List<TripApproveDTO> tripApproveDTOS = new ArrayList<>(
                controllerService.fillLimits(controllerService.fillTripPurpose(approvals.getContent()), departmentId, "Bearer " + authentication.getToken().getTokenValue(), false));
        
        return new PageImpl<>(
                tripApproveDTOS,
                approvals.getPageable(),
                approvals.getTotalElements());
    }
    
    @Override
    public Page<TripApproveDTO> getPageClosedApprovals(Pageable page) {
        final UUID userId = getUserIdFromAuthentication();
        var approvals = tripApproveService.getClosedApprovalsForUser(userId, page);
        List<TripApproveDTO> tripApproveDTOS = new ArrayList<>(controllerService.fillTripPurpose(
                approvals.stream().map(TripApproveDTO.class::cast).toList()));
        
        return new PageImpl<>(
                tripApproveDTOS,
                approvals.getPageable(),
                approvals.getTotalElements());
    }
    
    @Override
    public Collection<TripApproveDTO> getActiveApprovals(@E2EUser("principal") JwtAuthenticationToken authentication) {
        var userId = getUserIdFromAuthentication();
        var departmentId = employeeService.getDepartmentIdByUserId(userId);
        var approvals = tripApproveService.getActiveApprovalsForUser(userId);
        var tripApproveDTOList = controllerService.fillTripPurpose(approvals).stream().map(TripApproveDTO.class::cast).toList();
        return controllerService.fillLimits(tripApproveDTOList, departmentId, "Bearer " + authentication.getToken().getTokenValue(), false);
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
