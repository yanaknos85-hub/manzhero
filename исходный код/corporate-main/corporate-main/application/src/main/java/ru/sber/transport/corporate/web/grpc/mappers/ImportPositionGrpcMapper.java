package ru.sber.transport.corporate.web.grpc.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.corporate.business.model.Position;
import ru.sber.transport.corporate.sync.grpc.service.State;
import ru.sber.transport.corporate.sync_import.grpc.service.Import;

/**
 * Маппер данных бизнес - gRPC.
 */
@Mapper(uses = {NullableMapper.class, ActiveStatusGrpcMapper.class})
public interface ImportPositionGrpcMapper extends ImportGrpcMapper<Import.PositionRequest, Position> {

    /**
     * Преобразование gRPC в бизнес.
     *
     * @param source gRPC.
     * @return бизнес.
     */
    @Mapping(target = "status", source = "active")
    @Mapping(target = "structureType", constant = "INTERNAL")
    @Mapping(target = "noApproveRequired", source = "chief")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    Position toBusiness(State.Position source);

    /**
     * Преобразование gRPC в бизнес.
     *
     * @param source gRPC.
     * @return бизнес.
     */
    @Mapping(target = "structureType", constant = "INTERNAL")
    @Mapping(target = "humanReadableId", ignore = true)
    Position toBusiness(Import.PositionRequest source);

}
