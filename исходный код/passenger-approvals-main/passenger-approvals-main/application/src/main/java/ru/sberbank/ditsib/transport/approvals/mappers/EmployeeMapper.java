package ru.sberbank.ditsib.transport.approvals.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sberbank.ditsib.transport.approvals.database.model.Employee;
import ru.sberbank.ditsib.transport.approvals.dto.EmployeeDTO;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

/**
 * Маппер сотрудников.
 */
@Mapper
public interface EmployeeMapper {
    
    Employee toModel(EmployeeMessage message, @MappingTarget Employee employee);
    
    EmployeeDTO toDTO(Employee employee);

    @Mapping(source = "delegatedById", target = "delegatedBy")
    Employee employeeMessageToEmployee(ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage source);
}
