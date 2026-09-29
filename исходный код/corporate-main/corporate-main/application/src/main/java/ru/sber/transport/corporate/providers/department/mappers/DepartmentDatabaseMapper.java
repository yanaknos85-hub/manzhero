package ru.sber.transport.corporate.providers.department.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.providers.mappers.DatabaseMapper;
import ru.sber.transport.database.corporate.tables.records.DepartmentRecord;

/**
 * Маппер бизнес объектов в базу данных.
 */
@Mapper
public interface DepartmentDatabaseMapper extends DatabaseMapper<Department, DepartmentRecord> {

    @Mapping(target = "fosType", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "humanReadableId", source = "humanreadableid")
    @Mapping(target = "headId", source = "head")
    @Mapping(target = "structureType", source = "orgStructureType")
    @Mapping(target = "handmade", source = "isHandmade")
    Department toBusiness(DepartmentRecord source);

    @Mapping(target = "humanreadableid", source = "humanReadableId")
    @Mapping(target = "orgStructureType", source = "structureType")
    @Mapping(target = "status", source = "status", defaultValue = "ACTIVE")
    @Mapping(target = "head", source = "headId")
    @Mapping(target = "isHandmade", source = "handmade")
    @Mapping(target = "table", ignore = true)
    @Mapping(target = "qualifier", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    DepartmentRecord toDatabase(Department source);

    @Mapping(target = "humanreadableid", source = "humanReadableId")
    @Mapping(target = "orgStructureType", source = "structureType")
    @Mapping(target = "status", source = "status", defaultValue = "ACTIVE")
    @Mapping(target = "head", source = "headId")
    @Mapping(target = "isHandmade", source = "handmade")
    @Mapping(target = "table", ignore = true)
    @Mapping(target = "qualifier", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void update(@MappingTarget DepartmentRecord target, Department source);
}
