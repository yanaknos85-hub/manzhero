package ru.sber.transport.corporate_sync.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.messaging.mappers.ActiveMessageMapper;
import ru.sber.transport.messages.easup.avro.EmployeeData;

/**
 * Маппер сообщений сотрудника
 */
@Mapper(uses = ActiveMessageMapper.class)
public interface EmployeeSyncMessageMapper extends MessageMapper<Employee, EmployeeData> {

    /**
     * Конвертация сообщения в бизнес
     *
     * @param source сообщение
     * @param target объект для обновления
     */
    @Override
    @Mapping(target = "approvals", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "departmentHead", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "departmentId", ignore = true)
    @Mapping(target = "positionId", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itinerant", source = "itinerantType")
    @Mapping(target = "managedDepartments", ignore = true)
    @Mapping(target = "marriageCertificate", source = "marriageCertificateId")
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "positionName", ignore = true)
    @Mapping(target = "status", source = "active")
    @Mapping(target = "structureType", constant = "INTERNAL")
    @Mapping(target = "type", constant = "INTERNAL")
    @Mapping(target = "supervisorId", ignore = true)
    @Mapping(target = "syncId", source = "personnelNumber")
    void update(@MappingTarget Employee target, EmployeeData source);

    /**
     * Конвертация сообщения в бизнес
     *
     * @param source сообщение
     * @return объект для обновления
     */
    @Override
    @Mapping(target = "approvals", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "departmentHead", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "departmentId", ignore = true)
    @Mapping(target = "positionId", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itinerant", source = "itinerantType")
    @Mapping(target = "managedDepartments", ignore = true)
    @Mapping(target = "marriageCertificate", source = "marriageCertificateId")
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "positionName", ignore = true)
    @Mapping(target = "status", source = "active")
    @Mapping(target = "structureType", constant = "INTERNAL")
    @Mapping(target = "type", constant = "INTERNAL")
    @Mapping(target = "supervisorId", ignore = true)
    @Mapping(target = "syncId", source = "personnelNumber")
    Employee toBusiness(EmployeeData source);
}
