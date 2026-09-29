package ru.sberbank.ditsib.transport.limits.controller.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.controller.EmpLimitController;
import ru.sberbank.ditsib.transport.limits.dto.EmpLimitSharingDTO;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.mapper.LimitSharingMapper;
import ru.sberbank.ditsib.transport.limits.model.GetLimitDTO;
import ru.sberbank.ditsib.transport.limits.model.LimitData;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ru.sberbank.ditsib.transport.constants.limits.LimitServiceType.getLimitServiceTypeByTransportType;


/**
 * Implementation of limit controller service.
 */
@RestController
@Transactional
@Scope("request")
class EmpLimitControllerImpl extends BaseController implements EmpLimitController {
    
    private final LimitService limitService;
    
    private final DepLimitService depLimitService;
    
    private final EmpLimitService empLimitService;
    
    private final EmployeeService employeeService;
    
    private final LimitSharingService limitSharingService;
    
    private final Map<LimitSharingType, LimitSharingPerPeriodService<? extends Period>> limitSharingPerPeriodServices;

    public EmpLimitControllerImpl(LimitSharingMapper limitSharingMapper, LimitService limitService, DepLimitService depLimitService, EmpLimitService empLimitService, EmployeeService employeeService, LimitSharingService limitSharingService, Map<LimitSharingType, LimitSharingPerPeriodService<? extends Period>> limitSharingPerPeriodServices) {
        super(limitSharingMapper);
        this.limitService = limitService;
        this.depLimitService = depLimitService;
        this.empLimitService = empLimitService;
        this.employeeService = employeeService;
        this.limitSharingService = limitSharingService;
        this.limitSharingPerPeriodServices = limitSharingPerPeriodServices;
    }

    @Override
    public GetLimitDTO get(UUID limitId) {
        var empLimit = empLimitService.get(limitId).orElseThrow(() -> new EntityNotFoundException(Limit.class, limitId));
        return transformLimitEntityToDTO(empLimit, employeeService);
    }
    
    @Override
    public List<GetLimitDTO> getAll() {
        List<EmpLimit> list = empLimitService.getAll();
        return list.stream().map(empLimit -> transformLimitEntityToDTO(empLimit, employeeService)).toList();
    }
    
    @Override
    public List<GetLimitDTO> getByEmployee(UUID employeeId) {
        var employee = employeeService.get(employeeId)
                                      .orElseThrow(() -> new EntityNotFoundException(Employee.class, employeeId));
        List<EmpLimit> list = empLimitService.getByEmployee(employee);
        return list.stream().map(empLimit -> transformLimitEntityToDTO(empLimit, employeeService)).toList();
    }
    
    @Override
    public GetLimitDTO getByEmployeeAndYear(UUID employeeId, Integer year) {
        var employee = employeeService.get(employeeId)
                                      .orElseThrow(() -> new EntityNotFoundException(Employee.class, employeeId));
        
        return transformLimitEntityToDTO(empLimitService.getByEmployeeAndYear(employee, year), employeeService);
    }
    
    @Override // целевой метод включает servicetype
    public GetLimitDTO getByEmployeeAndYearFull(UUID employeeId, Integer year) {
        var employee = employeeService.get(employeeId)
                                      .orElseThrow(() -> new EntityNotFoundException(Employee.class, employeeId));
        
        var empLimit = empLimitService.getByEmployeeAndYear(employee, year);
        if (empLimit != null) {
            var getLimitDto = transformLimitEntityToDTO(empLimit, employeeService);
            var limitSharingPerPeriodService = limitSharingPerPeriodServices.get(empLimit.getLimitSharingType());
            if (getLimitDto != null) {
                List<LimitSharing> list = limitSharingService.getByLimit(empLimit);
                getLimitDto.setLimitSharingDTOList(convertToEnrichedLimitSharingDTOList(list,
                        limitSharingPerPeriodService));
            }
            return getLimitDto;
        }
        return null;
    }
    
    @Override
    public void makeEmployeeLimits(List<EmpLimitSharingDTO> limitSharingDTOList,
                                   JwtAuthenticationToken authentication) {
        var author = getEmployee(authentication);
        for (EmpLimitSharingDTO limitSharingDTO : limitSharingDTOList) {
            makeEmployeeLimit(limitSharingDTO, author);
        }
    }
    
    @Override
    public void makeEmployeeLimit(EmpLimitSharingDTO limitSharingDTO, JwtAuthenticationToken authentication) {
        makeEmployeeLimit(limitSharingDTO, getEmployee(authentication));
    }
    
    private void makeEmployeeLimit(EmpLimitSharingDTO limitSharingDTO, Employee author) {
        int currentYear = LocalDate.now(ZoneOffset.UTC).getYear();
        if (limitSharingDTO.getYear() < currentYear) {
            throw new LimitLogicException("Лимит не может быть выделен за прошедшие годы");
        }
    
        Employee targetEmployee = employeeService.get(limitSharingDTO.getTargetEmployeeId()).orElseThrow(
                () -> new EntityNotFoundException(Employee.class, limitSharingDTO.getTargetEmployeeId()));
    
        DepLimit parentDepLimit = depLimitService.getByDepartmentAndYearAndLimitServiceType(
                targetEmployee.getDepartmentId(),
                limitSharingDTO.getYear(),
                getLimitServiceTypeByTransportType(limitSharingDTO.getTransportType()).name());
        if (parentDepLimit == null) {
            throw new EntityNotFoundException(Department.class, targetEmployee.getDepartmentId());
        }
        if (!parentDepLimit.getLimitStatus().equals(LimitStatus.SHARED)) {
            throw new LimitLogicException("Личный лимит может быть выделен только из лимита подразделения в статусе распределение");
        }
    
        empLimitService.createEmpLimitAndTransfer(targetEmployee, author, parentDepLimit,
                                                  limitSharingDTO.getSum(),
                                                  limitSharingDTO.getTransportType());
    }
    
    @Override
    public void changeEmployeeLimit(EmpLimitSharingDTO limitSharingDTO, JwtAuthenticationToken authentication) {
        var author = getEmployee(authentication);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        final var limitServiceType = getLimitServiceTypeByTransportType(limitSharingDTO.getTransportType()).name();
        if (limitSharingDTO.getYear() < currentYear) {
            throw new LimitLogicException("Лимит за прошедшие годы не может быть изменен");
        }
        Employee targetEmployee = employeeService.get(limitSharingDTO.getTargetEmployeeId()).orElseThrow(
                () -> new EntityNotFoundException(Employee.class, limitSharingDTO.getTargetEmployeeId()));
        DepLimit parentDepLimit = depLimitService.getByDepartmentAndYearAndLimitServiceType(
                targetEmployee.getDepartmentId(),
                limitSharingDTO.getYear(),
                limitServiceType);
        if (parentDepLimit == null) {
            throw new EntityNotFoundException(Department.class, targetEmployee.getDepartmentId());
        }
        EmpLimit targetEmpLimit = empLimitService.getByEmployeeAndYearAndLimitServiceType(targetEmployee,
                                                                       limitSharingDTO.getYear(), limitServiceType);
        if (targetEmpLimit == null) {
            throw new EntityNotFoundException(Employee.class, targetEmployee.getId());
        }
        if (!parentDepLimit.getLimitStatus().equals(LimitStatus.SHARED)) {
            throw new LimitLogicException("Личный лимит может быть выделен только из лимита подразделения в статусе распределение");
        }
        
        final var sum = limitSharingDTO.getSum();
        TransportTypeEnum transportType = limitSharingDTO.getTransportType();
        if (sum.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        LimitData source;
        LimitData target;
        if (sum.compareTo(BigDecimal.ZERO) > 0) { // we want to augment employee limit from dep limit
            source = new LimitData(parentDepLimit, transportType, null);
            target = new LimitData(targetEmpLimit, transportType, null);
        } else { // we want to diminish employee limit and add its part to dep limit
            source = new LimitData(targetEmpLimit, transportType, null);
            target = new LimitData(parentDepLimit, transportType, null);
        }
        limitService.transferSum(source, target, sum.abs(), author.getId(), false);
    }
    
    @Override
    public void closeEmployeeLimit(UUID empLimitId, JwtAuthenticationToken authentication) {
        var author = getEmployee(authentication);
    
        var empLimit = empLimitService.get(empLimitId).orElseThrow(() -> new EntityNotFoundException(Limit.class, empLimitId));
        empLimitService.closeEmpLimit(empLimit, author.getId());
    }

    private Employee getEmployee(JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        return employeeService.getByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, Map.of("userId", userId)));
    }
    
    
}
