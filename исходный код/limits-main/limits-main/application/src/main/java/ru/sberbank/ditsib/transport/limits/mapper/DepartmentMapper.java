package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetDepartmentV2DTO;
import ru.sberbank.ditsib.transport.limits.model.GetDepartmentDTO;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;

@Mapper
public interface DepartmentMapper {

    @Mapping(target = "name", source = "departmentName")
    GetDepartmentV2DTO toDto(Department department);

    @Mapping(target = "departmentName", source = "name")
    GetDepartmentDTO toDto(ru.sber.transport.limits.business.model.Department value);
}
