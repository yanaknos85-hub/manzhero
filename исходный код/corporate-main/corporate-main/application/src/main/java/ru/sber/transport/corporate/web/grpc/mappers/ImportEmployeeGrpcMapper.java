package ru.sber.transport.corporate.web.grpc.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.sync.grpc.service.State;
import ru.sber.transport.corporate.sync_import.grpc.service.Import;

/**
 * Маппер данных о сотруднике из ЕАСУП.
 */
@Mapper(uses = {ActiveStatusGrpcMapper.class, ItinerantGrpcMapper.class, NullableMapper.class, GenderGrpcMapper.class, DateGrpcMapper.class})
public interface ImportEmployeeGrpcMapper extends ImportGrpcMapper<Import.EmployeeRequest, Employee> {

    /**
     * Преобразование gRPC в бизнес.
     *
     * @param source gRPC.
     * @return бизнес.
     */
    @Mapping(target = "itinerant", source = "itinerantType")
    @Mapping(target = "type", constant = "INTERNAL")
    @Mapping(target = "structureType", constant = "INTERNAL")
    @Mapping(target = "syncId", source = "personnelNumber")
    @Mapping(target = "status", source = "active")
    @Mapping(target = "departmentId", ignore = true)
    @Mapping(target = "positionId", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "approvals", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "departmentHead", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "managedDepartments", ignore = true)
    @Mapping(target = "marriageCertificate", source = "marriageCertificateId")
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "positionName", ignore = true)
    @Mapping(target = "supervisorId", ignore = true)
    Employee toBusiness(State.Employee source);

    /**
     * Преобразование gRPC в бизнес.
     *
     * @param source gRPC.
     * @return бизнес.
     */
    @Mapping(target = "type", constant = "INTERNAL")
    @Mapping(target = "structureType", constant = "INTERNAL")
    @Mapping(target = "syncId", source = "personnelNumber")
    @Mapping(target = "phone", source = "mobilePhone")
    @Mapping(target = "approvals", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "departmentHead", ignore = true)
    @Mapping(target = "managedDepartments", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "positionName", ignore = true)
    Employee toBusiness(Import.EmployeeRequest source);

}
