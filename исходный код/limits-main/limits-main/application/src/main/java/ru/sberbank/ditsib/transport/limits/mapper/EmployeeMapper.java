package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sberbank.ditsib.transport.limits.model.GetEmployeeDTO;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;

import java.util.Optional;
import java.util.UUID;

@Mapper
public interface EmployeeMapper {
    
    default UUID map(Employee source) {
        return Optional.ofNullable(source).map(Employee::getId).orElse(null);
    }

    GetEmployeeDTO mapToDto(Employee source);

    GetEmployeeDTO mapToDto(ru.sber.transport.limits.business.model.Employee source);

    @Named("mapEmployee")
    default GetEmployeeDTO mapEmployee(UUID id) {
        return employeeService().get(id).map(this::mapToDto).orElse(null);
    }

    @Lookup
    default EmployeeService employeeService() {
        return null;
    }
}
