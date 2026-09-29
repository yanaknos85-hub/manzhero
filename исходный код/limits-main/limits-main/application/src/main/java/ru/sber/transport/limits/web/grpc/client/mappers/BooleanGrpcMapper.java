package ru.sber.transport.limits.web.grpc.client.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper
public interface BooleanGrpcMapper {

    String INVERT_METHOD = "invert";

    @Named(INVERT_METHOD)
    default Boolean invert(Boolean source) {
        if (source == null) {
            return null;
        }
        return !source;
    }
}
