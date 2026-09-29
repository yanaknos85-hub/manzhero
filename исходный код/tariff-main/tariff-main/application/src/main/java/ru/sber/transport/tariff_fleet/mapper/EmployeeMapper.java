package ru.sber.transport.tariff_fleet.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.sber.transport.tariff_fleet.database.model.Department;
import ru.sber.transport.tariff_fleet.database.model.Employee;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

/**
 * Маппер сотрудников.
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = Department.class)
public interface EmployeeMapper {

    @Mapping(source = "departmentId", target = "department.id")
    @Mapping(source = "positionId", target = "position.id")
    @Mapping(source = "organizationId", target = "organization.id")
    Employee employeeMessageToEmployee(EmployeeMessage source);
}
