package ru.sberbank.ditsib.corpclient.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.corpclient.database.model.RoleActionType;
import ru.sberbank.ditsib.corpclient.service.EmployeeService;
import ru.sberbank.ditsib.corpclient.service.RoleHistoryService;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Аспект для аудита изменения роли курьера через CourierController.
 * Сохраняет запись в истории при добавлении/удалении роли курьера.
 * Ошибки при сохранении истории не влияют на основную транзакцию.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
class CourierRoleAuditAspect {

    private static final String ROLE_COURIER = "ROLE_COURIER";
    private final RoleHistoryService roleHistoryService;

    /**
     * Pointcut для методов контроллера курьера.
     */
    @Pointcut("execution(* ru.sberbank.ditsib.corpclient.controller.impl.CourierControllerImpl.addCourierRole(..)) && args(authentication)")
    public void addCourierRolePointcut(JwtAuthenticationToken authentication) {
    }

    @Pointcut("execution(* ru.sberbank.ditsib.corpclient.controller.impl.CourierControllerImpl.deleteCourierRole(..)) && args(authentication)")
    public void deleteCourierRolePointcut(JwtAuthenticationToken authentication) {
    }

    /**
     * Обработка добавления роли курьера после успешного выполнения.
     */
    @AfterReturning(pointcut = "addCourierRolePointcut(authentication)")
    public void afterAddCourierRole(JwtAuthenticationToken authentication) {
        handleRoleChange(authentication, true, RoleActionType.ADD_COURIER, "Добавление роли курьера");
    }

    /**
     * Обработка удаления роли курьера после успешного выполнения.
     */
    @AfterReturning(pointcut = "deleteCourierRolePointcut(authentication)")
    public void afterDeleteCourierRole(JwtAuthenticationToken authentication) {
        handleRoleChange(authentication, false, RoleActionType.REMOVE_COURIER, "Удаление роли курьера");
    }

    private void handleRoleChange(JwtAuthenticationToken authentication, boolean isAddition, RoleActionType actionType, String comment) {
        var userId = UUID.fromString(authentication.getToken().getId());
        var currentRoles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        var hasRole = currentRoles.contains(ROLE_COURIER);
        if (isAddition && !hasRole) {
            currentRoles.add(ROLE_COURIER);
            saveHistoryWithLogging(userId, currentRoles.toString(), actionType, comment);
        } else if(!isAddition && hasRole) {
            currentRoles.remove(ROLE_COURIER);
            saveHistoryWithLogging(userId, currentRoles.toString(), actionType, comment);
        }
    }

    private void saveHistoryWithLogging(UUID employeeId, String roleNames, RoleActionType actionType, String comment) {
        try {
            roleHistoryService.saveHistory(
                    employeeId,
                    roleNames,
                    actionType,
                    comment
            );
        } catch (Exception e) {
            log.error("CourierRoleAuditAspect: failed to save role history for employee={}, action={}, comment={}, error={}",
                    employeeId, actionType, comment, e.getMessage(), e);
        }
    }
}
