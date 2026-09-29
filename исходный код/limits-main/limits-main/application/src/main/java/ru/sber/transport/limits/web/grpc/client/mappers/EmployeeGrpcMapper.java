package ru.sber.transport.limits.web.grpc.client.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.limits.business.model.Employee;

import static ru.sber.transport.limits.web.grpc.client.mappers.BooleanGrpcMapper.INVERT_METHOD;

/**
 * Mapper для маппинга объектов в gRPC.
 */
@Mapper(uses = {ContactGrpcMapper.class, NullableGrpcMapper.class, BooleanGrpcMapper.class})
public interface EmployeeGrpcMapper {

    /**
     * Маппинг объекта в gRPC.
     *
     * @param source исходный объект
     * @return бизнес-объект
     */
    @Mapping(target = "email", source = "contactsList")
    @Mapping(target = "active", source = "deleted", qualifiedByName = INVERT_METHOD)
    @Mapping(target = "userId", source = "id")
    Employee toBusiness(OrganizationsOuterClass.Employee source);

}
