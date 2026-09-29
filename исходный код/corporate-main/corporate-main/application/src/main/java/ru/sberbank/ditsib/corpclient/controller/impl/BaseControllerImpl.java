package ru.sberbank.ditsib.corpclient.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sberbank.ditsib.corpclient.service.EmployeeService;

import java.util.UUID;

/**
 * Base controller implementation.
 */
@RequiredArgsConstructor
abstract class BaseControllerImpl {

    private final EmployeeService employeeService;
    
    /**
     * Check user existence.
     *
     * @param authentication authenticated user data.
     *
     * @return user ID.
     */
    protected UUID getEmployeeIdByUserId(JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        return employeeService.getEmployeeByUserId(userId).getId();
    }
    
}
