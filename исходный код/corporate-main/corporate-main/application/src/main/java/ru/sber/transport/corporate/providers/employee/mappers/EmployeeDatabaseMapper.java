package ru.sber.transport.corporate.providers.employee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.providers.mappers.DatabaseMapper;
import ru.sber.transport.database.corporate.tables.records.EmployeeRecord;
import ru.sber.transport.corporate.utils.Constants;

import java.time.OffsetDateTime;

/**
 * Маппер бизнес объектов в базу данных.
 */
@Mapper(imports = {OffsetDateTime.class, Constants.class})
public interface EmployeeDatabaseMapper extends DatabaseMapper<Employee, EmployeeRecord> {

    /**
     * Конвертировать модель базы данных в бизнес.
     *
     * @param source модель базы данных.
     * @return бизнес данные.
     */
    @Mapping(target = "humanReadableId", source = "humanreadableid")
    @Mapping(target = "itinerant", source = "itinerantType")
    @Mapping(target = "marriageCertificate", source = "marriageCertificateId")
    @Mapping(target = "phone", source = "mobilePhone")
    @Mapping(target = "status", source = "status", defaultValue = "ACTIVE")
    @Mapping(target = "type", source = "orgStructureType")
    @Mapping(target = "structureType", source = "orgStructureType")
    @Mapping(target = "syncId", source = "personnelNumber")
    @Mapping(target = "approvals", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "departmentHead", ignore = true)
    @Mapping(target = "managedDepartments", ignore = true)
    @Mapping(target = "positionName", ignore = true)
    Employee toBusiness(EmployeeRecord source);

    /**
     * Конвертировать бизнес в модель базы данных.
     *
     * @param target цель для обновления данные.
     * @param source бизнес данные.
     */
    @Mapping(target = "humanreadableid", source = "humanReadableId")
    @Mapping(target = "itinerantType", source = "itinerant")
    @Mapping(target = "marriageCertificateId", source = "marriageCertificate")
    @Mapping(target = "mobilePhone", source = "phone")
    @Mapping(target = "orgStructureType", source = "type")
    @Mapping(target = "updateTime", expression = "java(OffsetDateTime.now())")
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "table", ignore = true)
    @Mapping(target = "qualifier", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "_NeedUpdate", ignore = true)
    void update(@MappingTarget EmployeeRecord target, Employee source);

    /**
     * Конвертировать бизнес в модель базы данных.
     *
     * @param employee исходная сущность.
     * @return модель базы данных.
     */
    @Mapping(target = "humanreadableid", source = "humanReadableId")
    @Mapping(target = "itinerantType", source = "itinerant")
    @Mapping(target = "marriageCertificateId", source = "marriageCertificate")
    @Mapping(target = "mobilePhone", source = "phone")
    @Mapping(target = "orgStructureType", source = "type")
    @Mapping(target = "updateTime", expression = "java(OffsetDateTime.now())")
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "table", ignore = true)
    @Mapping(target = "qualifier", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "_NeedUpdate", ignore = true)
    EmployeeRecord toDatabase(Employee employee);
}
