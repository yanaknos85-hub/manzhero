package ru.sber.transport.limits.web.grpc.client.mappers;

import org.mapstruct.Mapper;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;

/**
 * Маппер обнуляемых значений в gRPC.
 */
@Mapper
public interface NullableGrpcMapper {

    /**
     * Преобразование обнуляемого значения gRPC в бизнес.
     * @param source исходные данные
     * @return бизнес-данные
     */
    default String toBusiness(OrganizationsOuterClass.NullableString source) {
        if (source.hasNull()) {
            return null;
        }
        return source.getValue();
    }

}
