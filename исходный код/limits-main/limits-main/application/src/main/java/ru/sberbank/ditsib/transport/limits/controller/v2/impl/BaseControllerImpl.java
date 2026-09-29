package ru.sberbank.ditsib.transport.limits.controller.v2.impl;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;

import java.util.Map;
import java.util.UUID;

abstract class BaseControllerImpl {

    protected Employee getEmployee(EmployeeService service, JwtAuthenticationToken authenticationToken) {
        var id = UUID.fromString(authenticationToken.getToken().getId());
        return service.getByUserId(id).orElseThrow(() -> new EntityNotFoundException(Employee.class, Map.of("userId", id)));
    }

}
