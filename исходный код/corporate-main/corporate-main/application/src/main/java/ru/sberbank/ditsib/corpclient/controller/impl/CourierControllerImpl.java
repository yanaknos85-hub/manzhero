package ru.sberbank.ditsib.corpclient.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.corpclient.controller.CourierController;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.messaging.sender.UserSender;
import ru.sberbank.ditsib.corpclient.service.EmployeeService;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CourierControllerImpl implements CourierController {

    private static final String ROLE_COURIER = "ROLE_COURIER";

    private final EmployeeService employeeService;

    private final UserSender userSender;

    @Override
    public void addCourierRole(JwtAuthenticationToken authentication) {
        Employee employee = getEmployee(authentication);
        Set<String> roles =
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());
        if (!roles.contains(ROLE_COURIER)) {
            roles.add(ROLE_COURIER);
            userSender.send(employee, roles);
        }
    }

    @Override
    public void deleteCourierRole(JwtAuthenticationToken authentication) {
        Employee employee = getEmployee(authentication);
        Set<String> roles =
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());
        if (roles.contains(ROLE_COURIER)) {
            roles.remove(ROLE_COURIER);
            userSender.send(employee, roles);
        }
    }

    private Employee getEmployee(JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        return employeeService.getEmployeeByUserId(userId);
    }
}
