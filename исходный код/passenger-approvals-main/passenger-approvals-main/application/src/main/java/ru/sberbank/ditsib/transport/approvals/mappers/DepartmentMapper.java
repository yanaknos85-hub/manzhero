package ru.sberbank.ditsib.transport.approvals.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.sberbank.ditsib.transport.approvals.database.model.Department;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

/**
 * Маппер сотрудников.
 */
@Mapper
public interface DepartmentMapper {

    void toModel(@MappingTarget Department target, DepartmentMessage message);

    Department departmentMessageToDepartment(DepartmentMessage source);
}
