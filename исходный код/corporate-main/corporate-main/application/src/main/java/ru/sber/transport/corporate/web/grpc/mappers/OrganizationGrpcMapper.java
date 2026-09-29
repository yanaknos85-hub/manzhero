package ru.sber.transport.corporate.web.grpc.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.sber.transport.corporate.business.model.Organization;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;

import static ru.sber.transport.corporate.web.grpc.mappers.ActiveStatusGrpcMapper.DELETED;

/**
 * Маппер данных бизнес - gRPC.
 */
@Mapper(imports = {OrganizationsOuterClass.StructureType.class}, uses = {ActiveStatusGrpcMapper.class, NullableMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrganizationGrpcMapper {

    /**
     * Преобразование бизнес в gRPC.
     *
     * @param source бизнес.
     * @return gRPC.
     */
    @Mapping(target = "type", expression = "java(source.getSyncId() == null ? StructureType.EXTERNAL : StructureType.INTERNAL)")
    @Mapping(target = "deleted", source = "status", qualifiedByName = DELETED)
    @Mapping(target = "group", source = "groupId")
    OrganizationsOuterClass.Organization toGrpc(Organization source);
}
