package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.services.DelegateService;
import ru.sberbank.ditsib.transport.approvals.services.DepartmentService;
import ru.sberbank.ditsib.transport.approvals.services.EmployeeRightService;
import ru.sberbank.ditsib.transport.approvals.services.EmployeeService;
import ru.sberbank.ditsib.transport.exceptions.IllegalCallerResponseException;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Implementation of employee rights service.
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class EmployeeRightServiceImpl implements EmployeeRightService {
    
    private final EmployeeService employeeService;
    
    private final DepartmentService departmentService;
    
    private final DelegateService delegateService;
    
    @Override
    public Map<String, Collection<UUID>> getAllowedDepartments(UUID userId) {
        
        var employee = employeeService.getEmployeeByIdWithApproveDepartments(userId);
        Map<String, Collection<UUID>> result = new HashMap<>();
        
        if (employee.getApproveDepartments() == null) {
            return result;
        }
        List<UUID> woTransportTypeRestrictions = employee.getApproveDepartments().stream()
                                                         .filter(Predicate.not(this::hasTransportTypeRestriction))
                                                         .map(ApproveDepartment::getDepartmentId)
                                                         .collect(Collectors.toList());
        
        Map<String, Set<UUID>> withTransportTypeRestrictions =
                employee.getApproveDepartments().stream()
                        .filter(this::hasTransportTypeRestriction)
                        .collect(Collectors.groupingBy(ApproveDepartment::getTransportType,
                                                       Collectors.mapping(ApproveDepartment::getDepartmentId,
                                                                          Collectors.toSet())));
        if (!woTransportTypeRestrictions.isEmpty()) {
            result.put(null, woTransportTypeRestrictions);
        }
        result.putAll(withTransportTypeRestrictions);
        return result;
        
    }
    
    private boolean hasTransportTypeRestriction(ApproveDepartment approveDepartment) {
        return approveDepartment.getTransportType() != null;
    }
    
    @Override
    public Employee checkEmployeeRights(UUID userId, BaseRequestApproval approval) {
        Employee employee = employeeService.getByUserId(userId);
        if (!canApprove(employee, approval)) {
            throw new IllegalCallerResponseException();
        }
        return employee;
    }
    
    private boolean canApprove(Employee approvedBy, BaseRequestApproval approval) {
        // Получаем список департаментов, которые может апрувить сотрудник approvedBy
        final var allowedDepartments = getAllowedDepartments(approvedBy.getUserId());
        // Получаем подразделение заявки, которую апрувят
        var actorDepartmentId = employeeService.get(approval.getActorId()).orElseThrow(
                () -> new EntityNotFoundException(Employee.class, approval.getActorId())).getDepartmentId();
        // Можно согласовать, если подразделение заявки находится в списке разрешенных для согласования
        // согласующего сотрудника
        boolean result = contains(actorDepartmentId, allowedDepartments.get(null)) ||
                    contains(actorDepartmentId, allowedDepartments.get(approval.getTransportType()));
        if (!result) {
            log.info("Allowed departments for employee " + approvedBy.getId() + ": " + allowedDepartments);
        }
        return result;
    }
    
    private boolean contains(UUID id, Collection<UUID> list) {
        return list != null && list.contains(id);
    }
    
    /**
     * Проверка возможности согласовать поездку.
     *
     * @param approval поездка для согласования.
     * @param approvedBy сотрудник, совершающий действие согласования.
     *
     * @return <code>true</code> если сотруднику позволено согласовать поездку.
     */
    @Deprecated
    private boolean canApprove(BaseRequestApproval approval, Employee approvedBy) {
        // todo Возможно, надо заменить данную логику на вызов getAllowedDepartments и проверкой approval на то, что
        //  он в списке разрешенных департаментов и типов транспорта
        UUID actorId = approval.getActorId();
        UUID approvedById = approvedBy.getId();
        Employee actorEmployee =
                employeeService.get(actorId).orElseThrow(() -> new EntityNotFoundException(Employee.class, actorId));
        
        Department department = getDepartment(actorEmployee.getDepartmentId());
        if (department == null) {
            throw new IllegalStateResponseException(
                    "Department not defined for employee with id = '" + actorEmployee.getId() + "'");
        }
        var checkedDepartments = new ArrayList<UUID>();// for prevent stackoverflow
        while (!canApproveByDepartment(approvedById, department)) {// Check by hierarchy
            checkedDepartments.add(department.getId());
            if (checkedDepartments.contains(department.getParentId())) {
                throw new IllegalStateResponseException("Department hierarchy is cyclic");
            }
            if (department.getParentId() == null) {
                return false;
            }
            department = getDepartment(department.getParentId());
            
        }
        return true;
    }
    
    @Deprecated
    private boolean canApproveByDepartment(UUID approvedById, Department department) {
        // todo Надо проверять является ли департамент владельцем лимита. Пока не сделано!!!
        // allow if approver is head of department
        if (Objects.equals(department.getDepartmentHeadId(), approvedById)) {
            return true;
        }
        // allow if approver is delegate of department head
        var delegates = delegateService.getDelegatesBySupervisor(department.getDepartmentHeadId());
        return delegates.stream()
                        .map(Delegate::getDelegateId)
                        .anyMatch(d -> Objects.equals(d, approvedById));
    }
    
    private Department getDepartment(UUID departmentId) {
        if (departmentId == null) {
            return null;
        }
        return departmentService.get(departmentId).orElseThrow(
                () -> new EntityNotFoundException(Department.class, departmentId));
    }
    
}
