package ru.sberbank.ditsib.corpclient.controller.impl;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sberbank.ditsib.corpclient.controller.PersonalCarController;
import ru.sberbank.ditsib.corpclient.dto.NewPersonalCarDTO;
import ru.sberbank.ditsib.corpclient.dto.PersonalCarDTO;
import ru.sberbank.ditsib.corpclient.messaging.sender.PersonalCarSender;
import ru.sberbank.ditsib.corpclient.service.*;

import java.util.Collection;
import java.util.UUID;

/**
 * Implementation of PersonalCarController
 */
@RestController
class PersonaCarControllerImpl extends BaseControllerImpl implements PersonalCarController {
    private final PersonalCarService service;
    private final EmployeeService employeeService;
    private final OrganizationService organizationService;
    private final DepartmentService departmentService;
    private final PersonalCarSender sender;

    public PersonaCarControllerImpl(EmployeeService employeeService, PersonalCarService service, OrganizationService organizationService, DepartmentService departmentService, PersonalCarSender sender) {
        super(employeeService);
        this.service = service;
        this.employeeService = employeeService;
        this.organizationService = organizationService;
        this.departmentService = departmentService;
        this.sender = sender;
    }

    @CheckOrganizationAccess
    @Override
    public PersonalCarDTO savePersonalCar(
            @Organization UUID orgId,
            UUID departmentId,
            UUID employeeId,
            NewPersonalCarDTO newPersonalAuto,
            JwtAuthenticationToken authentication
                                         ) {
        validateIds(orgId, departmentId, employeeId);
        newPersonalAuto.setEmployeeId(employeeId);
        var currentUser = getEmployeeIdByUserId(authentication);
        var newPersonalCarDTO = service.savePersonalCar(newPersonalAuto, currentUser);
        
        sender.send(newPersonalCarDTO, false);
        return newPersonalCarDTO;
    }
    
    @CheckOrganizationAccess
    @Override
    public void editPersonalAuto(
            @Organization UUID orgId,
            UUID departmentId,
            UUID employeeId,
            UUID autoId,
            PersonalCarDTO newData,
            JwtAuthenticationToken authentication
                                ) {
        getPersonalCar(orgId, departmentId, employeeId, autoId);
        newData.setEmployeeId(employeeId);
        var currentUser = getEmployeeIdByUserId(authentication);
        var updatedPersonalAuto = service.updatePersonalAuto(newData, currentUser);
        sender.send(updatedPersonalAuto, false);
    }
    
    @CheckOrganizationAccess
    @Override
    public void deletePersonalAuto(
            @Organization UUID orgId,
            UUID departmentId,
            UUID employeeId,
            UUID autoId
                                  ) {
        PersonalCarDTO deleted = getPersonalCar(orgId, departmentId, employeeId, autoId);
        service.deletePersonalAuto(autoId);
        sender.send(deleted, true);
    }
    
    @CheckOrganizationAccess
    @Override
    public PersonalCarDTO getPersonalCar(
            @Organization UUID orgId,
            UUID departmentId,
            UUID employeeId,
            UUID autoId
                                        ) {
        validateIds(orgId, departmentId, employeeId);
        return service.getPersonalCarByIdAndEmployeeId(autoId, employeeId);
    }
    
    @CheckOrganizationAccess
    @Override
    public Collection<PersonalCarDTO> getPersonalCars(
            @Organization UUID orgId,
            UUID departmentId,
            UUID employeeId
                                                               ) {
        validateIds(orgId, departmentId, employeeId);
        return service.getPersonalCarsByUser(employeeId);
    }
    
    private void validateIds(UUID orgId, UUID departmentId, UUID employeeId) {
        organizationService.validateOrganizationId(orgId);
        departmentService.validateDepartmentByIdAndOrgId(departmentId, orgId);
        employeeService.validateAndGetEmployeeByIdAndDepartmentId(employeeId, departmentId);
    }
    
    
}
