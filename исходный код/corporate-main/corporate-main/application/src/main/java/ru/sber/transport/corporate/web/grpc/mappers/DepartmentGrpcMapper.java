package ru.sber.transport.corporate.web.grpc.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;

import static ru.sber.transport.corporate.web.grpc.mappers.ActiveStatusGrpcMapper.DELETED;

/**
 * Маппер данных бизнес - gRPC.
 */
@Mapper(uses = {ActiveStatusGrpcMapper.class, NullableMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepartmentGrpcMapper extends GrpcMapper<Department, OrganizationsOuterClass.Department> {

    /**
     * Преобразование бизнес в gRPC.
     *
     * @param source бизнес.
     * @return gRPC.
     */
    @Mapping(target = "deleted", source = "status", qualifiedByName = DELETED)
    OrganizationsOuterClass.Department toGrpc(Department source);

}
