package ru.sber.transport.corporate.web.grpc.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.sber.transport.corporate.business.model.TripPurpose;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;

/**
 * Маппер данных бизнес - gRPC.
 */
@Mapper(imports = {OrganizationsOuterClass.StructureType.class}, uses = {ActiveStatusGrpcMapper.class, NullableMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TripPurposeGrpcMapper {

    /**
     * Преобразование бизнес в gRPC.
     *
     * @param source бизнес.
     * @return gRPC.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "label", source = "label")
    OrganizationsOuterClass.TripPurpose toGrpc(TripPurpose source);
}
