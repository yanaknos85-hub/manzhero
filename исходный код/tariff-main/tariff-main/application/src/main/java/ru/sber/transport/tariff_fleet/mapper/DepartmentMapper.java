package ru.sber.transport.tariff_fleet.mapper;

import org.mapstruct.Mapper;
import ru.sber.transport.tariff_fleet.database.model.Department;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

/**
 * Маппер подразделений.
 */
@Mapper(componentModel = "spring")
public interface DepartmentMapper {
    Department departmentMessageToDepartment(DepartmentMessage source);
}
