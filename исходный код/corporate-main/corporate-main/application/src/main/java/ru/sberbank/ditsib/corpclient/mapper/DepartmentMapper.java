package ru.sberbank.ditsib.corpclient.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.corpclient.database.model.ActiveStatus;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.dto.*;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.util.UUID;

/**
 * Маппер подразделений.
 */
@Mapper(uses = {DateMapper.class, ActiveStatusMapper.class, DateMapper.class})
public interface DepartmentMapper {

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "geozoneId", source = "geozone")
    @Mapping(target = "departmentHead", source = "head")
    @Mapping(target = "level", source = "source")
    @Mapping(target = "status", source = "activeStatus")
    @Mapping(target = "fullStructurePath", source = "name")
    @Mapping(target = "easupId", source = "syncId")
    DepartmentDTO departmentToDTO(Department source);

    @Mapping(target = "departmentName", source = "name")
    DepartmentParentDTO departmentParentToDTO(Department source);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "departmentHead", source = "head")
    @Mapping(target = "geozoneId", source = "geozone")
    @Mapping(target = "fullStructurePath", source = "name")
    @Mapping(target = "status", source = "activeStatus")
    @Mapping(target = "easupId", source = "syncId")
    DepartmentDTO departmentToDTONoLevel(Department source);

    default int extractLevel(Department source) {
        var i = 1;
        for (; source.getParent() != null; i++) {
            source = source.getParent();
        }
        return i;
    }

    @Mapping(target = "geozoneId", ignore = true)
    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "level", source = "source")
    @Mapping(target = "departmentHead", source = "head")
    @Mapping(target = "status", source = "activeStatus")
    @Mapping(target = "easupId", source = "syncId")
    DepartmentSelectDTO departmentToSelectDTO(Department source);


    @Mapping(target = "status", source = "activeStatus")
    @Mapping(target = "departmentName", source = "name")
    @Mapping(target = "headNameFirst", source = "head.firstName")
    @Mapping(target = "headNameLast", source = "head.lastName")
    @Mapping(target = "organizationName", source = "organization.officialName")
    @Mapping(target = "parentDepartmentName", source = "parent.name")
    @Mapping(target = "easupId", source = "syncId")
    DepartmentShortDTO departmentsToShortDTOs(Department source);

    ActiveStatus toModel(DepartmentStatus dto);

    @Mapping(target = "head", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "employees", ignore = true)
    @Mapping(target = "orgStructureType", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "isHandmade", constant = "true")
    @Mapping(source = "geozoneId", target = "geozone")
    @Mapping(target = "syncId", source = "easupId")
    @Mapping(target = "activeStatus", source = "status")
    Department newDTOToDepartment(NewDepartmentDTO source);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "departmentHeadId", source = "head.id")
    @Mapping(target = "departmentName", source = "name")
    @Mapping(target = "easupId", source = "syncId")
    @Mapping(target = "deleted", source = "activeStatus", qualifiedByName = "deleted")
    DepartmentMessage toMessage(Department department);

    @Mapping(target = "departmentName", source = "name")
    @Mapping(target = "status", source = "activeStatus")
    @Mapping(target = "easupId", source = "syncId")
    DepartmentExecutorGroupDTO departmentsToExecutorGroupDTO(Department department);

    @Mapping(target = "id", source = "uuid")
    Department departmentFromId(UUID uuid);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "headId", source = "head.id")
    @Mapping(target = "easupId", source = "syncId")
    @Mapping(target = "deleted", source = "activeStatus", qualifiedByName = "deleted")
    ru.sber.transport.messages.corporate.avro.DepartmentMessage toAvroMessage(Department value);
}