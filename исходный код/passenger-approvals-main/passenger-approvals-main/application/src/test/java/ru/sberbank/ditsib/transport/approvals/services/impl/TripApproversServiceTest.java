package ru.sberbank.ditsib.transport.approvals.services.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.messaging.senders.DepartmentTripRequestApproversSender;
import ru.sberbank.ditsib.transport.approvals.services.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Согласующие поездок")
public class TripApproversServiceTest {
    
    private final DepartmentService departmentService = mock(DepartmentService.class);
    
    private final DepLimitService limitService = mock(DepLimitService.class);
    
    private final EmployeeService employeeService = mock(EmployeeService.class);
    
    private final DelegateService delegateService = mock(DelegateService.class);
    
    private final DepartmentTripRequestApproversSender approversSender =
            mock(DepartmentTripRequestApproversSender.class);
    
    private final TripApproverService service = new TripApproverServiceImpl(departmentService, limitService,
                                                                            employeeService, delegateService,
                                                                            approversSender);
    
    /*
    Согласующий - руководитель подразделения-владелец лимита.
     */
    @Test
    @DisplayName("Получить согласующего")
    void should_returnApprover() {
        var departmentId = UUID.randomUUID();
        var employeeId = UUID.randomUUID();
        var approverId = UUID.randomUUID();
        
        var limit = new DepLimit();
        limit.setOwnerId(approverId);
        
        var approver = Employee.builder().id(approverId).build();
        
        var employee = Employee.builder().id(employeeId).departmentId(departmentId).build();
        
        var department = Department.builder().id(departmentId).departmentHeadId(approverId).build();
        when(departmentService.get(departmentId)).thenReturn(Optional.of(department));
        when(employeeService.get(employeeId)).thenReturn(Optional.of(employee));
        
        var actualList = service.computeApprovers(employee.getId());
        
        assertThat(actualList.size()).isEqualTo(1);
        assertThat(actualList.iterator().next().getEmployeeId()).isEqualTo(approverId);
    }
    
    @Test
    @DisplayName("Согласующий не зависит от наличия лимита")
    void should_returnInheritedApprovers() {
        var departmentId = UUID.randomUUID();
        var parentDepartmentId = UUID.randomUUID();
        var employeeId = UUID.randomUUID();
        var approverId = UUID.randomUUID();
        var headId = UUID.randomUUID();
        
        var limit = new DepLimit();
        limit.setOwnerId(approverId);
        
        var head = Employee.builder().id(headId).build();
        
        var approver = Employee.builder().id(approverId).build();
        
        var employee = Employee.builder().id(employeeId).departmentId(departmentId).build();
        
        var parentDepartment = Department.builder().id(parentDepartmentId).departmentHeadId(approverId).build();
        var department = Department.builder().id(departmentId).departmentHeadId(headId)
                                   .parentId(parentDepartmentId).build();
        when(departmentService.get(departmentId)).thenReturn(Optional.of(department));
        when(departmentService.get(parentDepartmentId)).thenReturn(Optional.of(parentDepartment));
        when(employeeService.get(employeeId)).thenReturn(Optional.of(employee));
        
        var actualList = service.computeApprovers(employee.getId());
        
        assertThat(actualList.size()).isEqualTo(1);
        assertThat(actualList.iterator().next().getEmployeeId()).isEqualTo(headId);
    }
    
    @Test
    @DisplayName("Руководитель уровня выше и его делегаты не являются согласующими")
    void should_returnInheritedApprovers_delegates() {
        var departmentId = UUID.randomUUID();
        var parentDepartmentId = UUID.randomUUID();
        var employeeId = UUID.randomUUID();
        var approverId = UUID.randomUUID();
        var headId = UUID.randomUUID();
        
        var limit = new DepLimit();
        limit.setOwnerId(approverId);
        
        var head = Employee.builder().id(headId).build();
        
        var approver = Employee.builder().id(approverId).build();
        
        var employee = Employee.builder().id(employeeId).departmentId(departmentId).build();
        
        var parentDepartment = Department.builder().id(parentDepartmentId).departmentHeadId(approverId).build();
        var department = Department.builder().id(departmentId).departmentHeadId(headId)
                                   .parentId(parentDepartmentId).build();
        
        var delegate1 = new Delegate();
        delegate1.setDelegateId(UUID.randomUUID());
        delegate1.setSupervisorId(headId);
        delegate1.setTransportType("TAXI");
        
        var delegate1Employee = Employee.builder().id(delegate1.getDelegateId()).build();
        
        var delegate2 = new Delegate();
        delegate2.setDelegateId(UUID.randomUUID());
        delegate2.setSupervisorId(headId);
        delegate2.setTransportType("PERSONAL");
        
        var delegate2Employee = Employee.builder().id(delegate2.getDelegateId()).build();
        
        var delegate3 = new Delegate();
        delegate3.setDelegateId(UUID.randomUUID());
        delegate3.setSupervisorId(headId);
        delegate3.setTransportType("PUBLIC");
        
        var delegate3Employee = Employee.builder().id(delegate3.getDelegateId()).build();
        
        when(departmentService.get(departmentId)).thenReturn(Optional.of(department));
        when(departmentService.get(parentDepartmentId)).thenReturn(Optional.of(parentDepartment));
        when(employeeService.get(employeeId)).thenReturn(Optional.of(employee));
        when(delegateService.getDelegatesBySupervisor(headId)).thenReturn(new HashSet<>(List.of(
                delegate1, delegate2, delegate3
                                                                                                   )));
        
        var actualList = service.computeApprovers(employee.getId());
        
        assertThat(actualList.size()).isEqualTo(3);
        checkApproverExists(actualList, delegate1.getDelegateId(), delegate1.getTransportType());
        checkApproverExists(actualList, delegate2.getDelegateId(), delegate2.getTransportType());
        checkApproverExists(actualList, delegate3.getDelegateId(), delegate3.getTransportType());
    }
    
    private void checkApproverExists(Collection<Approver> approvers, UUID approverId, String transportType) {
        assertThat(approvers.stream()
                            .anyMatch(approver ->
                                              Objects.equals(approver.getEmployeeId(), approverId) &&
                                              Objects.equals(approver.getTransportType(), transportType))
                  ).isTrue();
    }
    
    @Test
    @DisplayName("Список пустой, так как подразделения еще нет")
    void testEmpty_departmentNotExists() {
        var departmentId = UUID.randomUUID();
        var employeeId = UUID.randomUUID();
        var approverId = UUID.randomUUID();
        
        var limit = new DepLimit();
        limit.setOwnerId(approverId);
        
        var approver = Employee.builder().id(approverId).build();
        
        var employee = Employee.builder().id(employeeId).departmentId(departmentId).build();
        
        when(departmentService.get(departmentId)).thenReturn(Optional.empty());
        when(employeeService.get(employeeId)).thenReturn(Optional.of(employee));
        
        var actualList = service.computeApprovers(employee.getId());
        
        assertThat(actualList.size()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Список пустой, так как employee еще нет")
    void testEmpty_employeeNotExists() {
        var departmentId = UUID.randomUUID();
        var employeeId = UUID.randomUUID();
        var approverId = UUID.randomUUID();
        
        var limit = new DepLimit();
        limit.setOwnerId(approverId);
        var department = Department.builder().id(departmentId).departmentHeadId(approverId).build();
        when(departmentService.get(departmentId)).thenReturn(Optional.of(department));
        when(employeeService.get(employeeId)).thenReturn(Optional.empty());
        
        var actualList = service.computeApprovers(employeeId);
        
        assertThat(actualList.size()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Список не пустой, так как согласующий не зависит от наличия department уровня выше или лимита")
    void testEmpty_parentDepartmentNotExists() {
        var departmentId = UUID.randomUUID();
        var parentDepartmentId = UUID.randomUUID();
        var employeeId = UUID.randomUUID();
        var approverId = UUID.randomUUID();
        var headId = UUID.randomUUID();
        
        var limit = new DepLimit();
        limit.setOwnerId(approverId);
        
        var employee = Employee.builder().id(employeeId).departmentId(departmentId).build();
        
        var department = Department.builder().id(departmentId).departmentHeadId(headId)
                                   .parentId(parentDepartmentId).build();
        when(departmentService.get(departmentId)).thenReturn(Optional.of(department));
        when(departmentService.get(parentDepartmentId)).thenReturn(Optional.empty());
        when(employeeService.get(employeeId)).thenReturn(Optional.of(employee));
        
        var actualList = service.computeApprovers(employee.getId());
        
        assertThat(actualList.size()).isEqualTo(1);
    }
    
}
