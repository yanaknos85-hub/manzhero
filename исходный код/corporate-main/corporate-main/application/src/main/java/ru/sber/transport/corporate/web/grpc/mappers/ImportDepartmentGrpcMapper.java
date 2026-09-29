package ru.sber.transport.corporate.web.grpc.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.sync.grpc.service.State;
import ru.sber.transport.corporate.sync_import.grpc.service.Import;

/**
 * Маппер данных бизнес - gRPC.
 */
@Mapper(uses = {NullableMapper.class, ActiveStatusGrpcMapper.class})
public interface ImportDepartmentGrpcMapper extends ImportGrpcMapper<Import.DepartmentRequest, Department> {

    /**
     * Преобразование gRPC в бизнес.
     *
     * @param source gRPC.
     * @return бизнес.
     */
    @Mapping(target = "status", source = "active")
    @Mapping(target = "code", source = "id")
    @Mapping(target = "structureType", constant = "INTERNAL")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "parentId", ignore = true)
    Department toBusiness(State.Department source);

    /**
     * Преобразование gRPC в бизнес.
     *
     * @param source gRPC.
     * @return бизнес.
     */
    @Mapping(target = "structureType", constant = "INTERNAL")
    @Mapping(target = "headId", source = "head")
    @Mapping(target = "updateDate", ignore = true)
    Department toBusiness(Import.DepartmentRequest source);

}
