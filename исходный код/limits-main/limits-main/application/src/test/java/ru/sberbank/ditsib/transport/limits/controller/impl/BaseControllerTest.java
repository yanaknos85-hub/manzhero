package ru.sberbank.ditsib.transport.limits.controller.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.EmpLimit;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
class BaseControllerTest {
    
    @DisplayName("В качестве владельца лимита подразделения возвращается руководитель подразделения (игнорируем limit_owner_id")
    @Test
    void transformLimitEntityToDTO_depLimit() {
        
        Employee limitOwner = Employee.builder().id(UUID.randomUUID()).build();
        Employee departmentHead = Employee.builder().id(UUID.randomUUID()).build();
        Department department = Department.builder().departmentHead(departmentHead).build();
        
        var limit = new DepLimit();
        limit.setLimitOwner(limitOwner);
        limit.setDepartment(department);
        
        EmployeeService employeeService = mock(EmployeeService.class);
        when(employeeService.get(limitOwner.getId())).thenReturn(Optional.of(limitOwner));
        when(employeeService.get(departmentHead.getId())).thenReturn(Optional.of(departmentHead));
        
        var controller = new BaseController(null) {};
        var limitDTO = controller.transformLimitEntityToDTO(limit, employeeService);
        
        assertThat(limitDTO.getLimitOwner()).isEqualTo(departmentHead.getId());
        assertThat(limitDTO.getOwner().getId()).isEqualTo(departmentHead.getId());
    }
    
    @DisplayName("В качестве владельца персонального лимита возвращается сотрудник (игнорируем limit_owner_id")
    @Test
    void transformLimitEntityToDTO_empLimit() {
        
        Employee limitOwner = Employee.builder().id(UUID.randomUUID()).build();
        Employee employee = Employee.builder().id(UUID.randomUUID()).build();
        
        var limit = new EmpLimit();
        limit.setLimitOwner(limitOwner);
        limit.setEmployee(employee);
        
        EmployeeService employeeService = mock(EmployeeService.class);
        when(employeeService.get(limitOwner.getId())).thenReturn(Optional.of(limitOwner));
        when(employeeService.get(employee.getId())).thenReturn(Optional.of(employee));
        
        var controller = new BaseController(null) {};
        var limitDTO = controller.transformLimitEntityToDTO(limit, employeeService);
        
        assertThat(limitDTO.getLimitOwner()).isEqualTo(employee.getId());
        assertThat(limitDTO.getOwner().getId()).isEqualTo(employee.getId());
    }
}