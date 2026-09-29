package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.approvals.database.model.Approver;
import ru.sberbank.ditsib.transport.approvals.database.model.Delegate;
import ru.sberbank.ditsib.transport.approvals.database.model.Department;
import ru.sberbank.ditsib.transport.approvals.messaging.senders.DepartmentTripRequestApproversSender;
import ru.sberbank.ditsib.transport.approvals.services.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Реализация сервиса по работе с согласующими.
 */
@RequiredArgsConstructor
@Component
@Slf4j
@Transactional
class TripApproverServiceImpl implements TripApproverService {
    
    private final DepartmentService departmentService;
    
    private final DepLimitService limitService;
    
    private final EmployeeService employeeService;
    
    private final DelegateService delegateService;
    
    private final DepartmentTripRequestApproversSender sender;
    
    @Override
    public Collection<Approver> computeApprovers(UUID employeeId) {
        var employee = employeeService.get(employeeId);
        if (employee.isEmpty()) {
            log.warn("Not found employee with id {}", employeeId);
            return new ArrayList<>();
        }
        log.info("Employee id {}", employeeId);
        
        var departmentId = employee.get().getDepartmentId();
        var department = departmentService.get(departmentId);
        if (department.isEmpty()) {
            log.warn("Not found department with id {}", departmentId);
            return new ArrayList<>();
        }
        log.info("Department id {}", departmentId);
        
        return computeApprovers(department.get(), true);
    }
    
    
    @Override
    public void onDelegateChanged(UUID supervisorId) {
        // Изменение делегатов может вызвать изменения в списке согласующих департамента, владельцем которого
        // является supervisorId
        updateDepartmentApproversByHead(supervisorId);
    }
    
    @Override
    public void onDepartmentChanged(UUID departmentId) {
        var department = departmentService.get(departmentId);
        if (department.isEmpty()) {
            log.warn("Not found department with id = " + departmentId);
            return;
        }
        updateDepartmentApprovers(department.get());
    }
    
    private void updateDepartmentApproversByHead(UUID headId) {
        var departments = departmentService.findByDepartmentHeadId(headId);
        if (departments.isEmpty()) {
            log.warn("Not found department for owner id = " + headId);
            return;
        }
        departments.forEach(this::updateDepartmentApprovers);
    }
    
    /**
     * Обновить согласующих для подразделения
     *
     * @param department переданное подразделение
     */
    private void updateDepartmentApprovers(Department department) {
        department.setApprovers(computeApprovers(department, false));
        sender.send(department.getId(), department.getApprovers());
    }
    
    private Collection<Approver> computeApprovers(Department department, boolean onlyDelegatesIfExists) {
        UUID initialDepartmentId = department.getId();
        var head = department.getDepartmentHeadId();
        log.debug("Department head id {}", head);
        if (head == null) {
            log.warn("Not found approver for department {}", initialDepartmentId);
            return new ArrayList<>();
        }
        log.debug("For department {} found a head approver: {}", initialDepartmentId, head);
        var delegates = delegateService.getDelegatesBySupervisor(head);
        log.debug("For department {} found delegates: {}", initialDepartmentId, delegates);
        if(onlyDelegatesIfExists) {
            if(delegates.isEmpty()) {
                return List.of(toHeadApprover(head));
            } else {
                return delegates.stream().map(this::toDelegateApprover).toList();
            }
        } else {
            return Stream.concat(Stream.of(toHeadApprover(head)),
                                 delegates.stream().map(this::toDelegateApprover))
                         .toList();
        }
    }
    
    private Approver toHeadApprover(UUID employeeId) {
        return Approver.builder().employeeId(employeeId).transportType(null).build();
    }
    
    private Approver toDelegateApprover(Delegate delegate) {
        return Approver.builder().employeeId(delegate.getDelegateId()).transportType(delegate.getTransportType())
                       .build();
    }
}
