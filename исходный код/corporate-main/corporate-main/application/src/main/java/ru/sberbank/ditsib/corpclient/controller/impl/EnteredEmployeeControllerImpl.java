package ru.sberbank.ditsib.corpclient.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.corpclient.controller.EnteredEmployeeController;
import ru.sberbank.ditsib.corpclient.dto.EmployeePatchField;
import ru.sberbank.ditsib.corpclient.dto.PatchData;
import ru.sberbank.ditsib.corpclient.messaging.sender.EmployeeSender;
import ru.sberbank.ditsib.corpclient.service.EmployeeService;
import ru.sberbank.ditsib.corpclient.service.PositionService;
import ru.sberbank.ditsib.corpclient.util.ContextHelper;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of entered employee controller.
 */
@RequiredArgsConstructor
@RestController
class EnteredEmployeeControllerImpl implements EnteredEmployeeController {
    
    private final EmployeeService employeeService;
    
    private final PositionService positionService;
    
    private final EmployeeSender employeeSender;

    @Override
    public void updateEmployee(List<PatchData<EmployeePatchField>> data, JwtAuthenticationToken authentication) {
        var authenticated = UUID.fromString(authentication.getToken().getId());
        var employee = employeeService.getEmployeeByUserId(authenticated);
        employeeService.updateEmployee(employee, data.stream().collect(Collectors.toMap(PatchData::field, PatchData::value)));
    }

    @Override
    public void signPdn(JwtAuthenticationToken authentication) {
        var authenticated = UUID.fromString(authentication.getToken().getId());
        employeeService.signPdn(authenticated);
    }

    @Override
    public Set<TaxiClass> getAllowedTaxiClasses() {
        var employeeByUserId = employeeService.getEmployeeByUserId(UUID.fromString(
                Objects.requireNonNull(ContextHelper.getCurrentUser())));
        return positionService.getPosition(Objects.requireNonNull(employeeByUserId.getId())).getAvailableClasses();
    }
    
    @Override
    public void resendEmployees() {
        var employeeDTOList = employeeService.getAllEmployees();
        employeeDTOList.forEach(employeeSender::send);
    }
}
